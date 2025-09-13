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

package net.pkhapps.vera.server;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import net.pkhapps.vera.server.domain.base.Aggregate;
import net.pkhapps.vera.server.domain.base.Repository;
import net.pkhapps.vera.server.util.wal.WriteAheadLog;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = ArchitectureTest.BASE)
class ArchitectureTest {

    static final String BASE = "net.pkhapps.vera.server";
    static final String PORTS = BASE + ".port..";
    static final String DOMAIN = BASE + ".domain..";
    static final String ADAPTERS = BASE + ".adapter..";
    static final String UTILS = BASE + ".util..";

    // BASE package is included because the Application class brings in classes from all packages (as it should).

    @ArchTest
    public static final ArchRule only_ports_and_adapters_can_depend_on_jackson = noClasses()
            .that().resideOutsideOfPackages(BASE, PORTS, ADAPTERS)
            .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..");

    @ArchTest
    public static final ArchRule only_adapters_can_depend_on_javalin = noClasses()
            .that().resideOutsideOfPackages(BASE, ADAPTERS)
            .should().dependOnClassesThat().resideInAPackage("io.javalin");

    @ArchTest
    public static final ArchRule only_ports_and_domain_can_access_repositories = noClasses()
            .that().resideOutsideOfPackages(BASE, PORTS, DOMAIN)
            .should().dependOnClassesThat().areAssignableTo(Repository.class);

    @ArchTest
    public static final ArchRule only_ports_and_domain_can_access_aggregates = noClasses()
            .that().resideOutsideOfPackages(BASE, PORTS, DOMAIN)
            .should().dependOnClassesThat().areAssignableTo(Aggregate.class);

    @ArchTest
    public static final ArchRule only_utils_and_domain_can_access_wal = noClasses()
            .that().resideOutsideOfPackages(BASE, UTILS, DOMAIN)
            .should().dependOnClassesThat().areAssignableTo(WriteAheadLog.class);
}
