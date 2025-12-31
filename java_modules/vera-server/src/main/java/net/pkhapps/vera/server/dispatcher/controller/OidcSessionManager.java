/*
 * Copyright (c) 2025 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.pkhapps.vera.server.dispatcher.controller;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import net.pkhapps.vera.security.AuthenticationFailedException;
import net.pkhapps.vera.server.dispatcher.internal.DispatcherPrincipal;
import net.pkhapps.vera.server.util.Registration;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class OidcSessionManager {

    private static final Logger log = LoggerFactory.getLogger(OidcSessionManager.class);
    private final ConfigurableJWTProcessor<SecurityContext> accessTokenProcessor;
    private final ConfigurableJWTProcessor<SecurityContext> logoutTokenProcessor;
    private final String introspectionUri;
    private final ConcurrentMap<String, PrincipalRevocationHandlers> revocationHandlers = new ConcurrentHashMap<>();

    OidcSessionManager(URL jwksUrl, URL issuerUrl, String introspectionUri) {
        JWKSource<SecurityContext> keySource = JWKSourceBuilder
                .create(jwksUrl)
                .retrying(true)
                .build();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                JWSAlgorithm.RS256,
                keySource);

        accessTokenProcessor = new DefaultJWTProcessor<>();
        accessTokenProcessor.setJWSKeySelector(keySelector);
        accessTokenProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuerUrl.toString()).build(),
                Set.of(
                        JWTClaimNames.SUBJECT,
                        JWTClaimNames.ISSUED_AT,
                        JWTClaimNames.EXPIRATION_TIME,
                        "sid",
                        "realm_access",
                        JWTClaimNames.JWT_ID)));

        logoutTokenProcessor = new DefaultJWTProcessor<>();
        logoutTokenProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("logout+jwt")));
        logoutTokenProcessor.setJWSKeySelector(keySelector);
        logoutTokenProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuerUrl.toString()).build(),
                Set.of(
                        JWTClaimNames.SUBJECT,
                        JWTClaimNames.ISSUED_AT,
                        JWTClaimNames.EXPIRATION_TIME,
                        "sid",
                        JWTClaimNames.JWT_ID)));

        this.introspectionUri = introspectionUri;
    }

    public DispatcherPrincipal processAccessToken(String token) {
        try {
            var claims = accessTokenProcessor.process(token, null);
            var sid = claims.getClaimAsString("sid");
            // TODO Make sure the token as the "dispatcher" role
            // TODO Call introspection endpoint, cache results
            return new DispatcherPrincipal(claims.getSubject(), sid);
        } catch (Exception e) {
            log.warn("Error verifying access token", e);
            throw new AuthenticationFailedException("Invalid access token");
        }
    }

    public void processLogoutToken(String token) {
        try {
            var claims = logoutTokenProcessor.process(token, null);
            var sid = claims.getClaimAsString("sid");
            log.info("Received logout token for sid={}", sid);
            // TODO Cache "bad" session ID for some time so that processAccessToken can query it
            var handlers = revocationHandlers.get(sid);
            if (handlers != null) {
                handlers.notifyHandlers();
            }
        } catch (Exception e) {
            log.warn("Error verifying logout token", e);
        }
    }

    public Registration registerPrincipalRevocationHandler(DispatcherPrincipal principal, Runnable onRevoke) {
        revocationHandlers.compute(principal.sid(), (_, currentHandlers) ->
                currentHandlers == null
                        ? new PrincipalRevocationHandlers(onRevoke)
                        : currentHandlers.with(onRevoke)
        );
        return () -> revocationHandlers.compute(principal.sid(), (_, currentHandlers) ->
                currentHandlers == null
                        ? null
                        : currentHandlers.remove(onRevoke)
        );
    }

    private static class PrincipalRevocationHandlers {

        private final Set<Runnable> handlers = new HashSet<>();

        public PrincipalRevocationHandlers(Runnable onRevoke) {
            handlers.add(onRevoke);
        }

        public synchronized PrincipalRevocationHandlers with(Runnable onRevoke) {
            handlers.add(onRevoke);
            return this;
        }

        public synchronized @Nullable PrincipalRevocationHandlers remove(Runnable onRevoke) {
            handlers.remove(onRevoke);
            return handlers.isEmpty() ? null : this;
        }

        public void notifyHandlers() {
            Set<Runnable> copyOfHandlers;
            synchronized (this) {
                copyOfHandlers = new HashSet<>(handlers);
            }
            copyOfHandlers.forEach(handler -> {
                try {
                    handler.run();
                } catch (Throwable e) {
                    log.error("Principal revocation handler threw an exception", e);
                }
            });
        }
    }
}
