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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Set;

class OidcSessionManager {

    private static final Logger log = LoggerFactory.getLogger(OidcSessionManager.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String introspectionUri;

    OidcSessionManager(URL jwksUrl, URL issuerUrl, String introspectionUri) {
        jwtProcessor = new DefaultJWTProcessor<>();
        //jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("Bearer")));
        JWKSource<SecurityContext> keySource = JWKSourceBuilder
                .create(jwksUrl)
                .retrying(true)
                .build();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                JWSAlgorithm.RS256,
                keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuerUrl.toString()).build(),
                Set.of(
                        JWTClaimNames.SUBJECT,
                        JWTClaimNames.ISSUED_AT,
                        JWTClaimNames.EXPIRATION_TIME,
                        "sid",
                        "realm_access",
                        JWTClaimNames.JWT_ID)));
        this.introspectionUri = introspectionUri;
    }

    public void invalidateOidcSession(String sid) {
        // TODO Notify listeners
    }

    public DispatcherPrincipal verifyOidcToken(String token) {
        try {
            var claims = jwtProcessor.process(token, null);
            return new DispatcherPrincipal(claims.getSubject());
        } catch (Exception e) {
            log.warn("Error verifying OIDC token", e);
            throw new AuthenticationFailedException("Invalid token");
        }
    }
}
