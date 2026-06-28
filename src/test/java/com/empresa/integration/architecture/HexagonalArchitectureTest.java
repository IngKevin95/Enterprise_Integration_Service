package com.empresa.integration.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Valida que las fronteras de la arquitectura hexagonal no se rompan. */
@AnalyzeClasses(packages = "com.empresa.integration")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainIsIsolated = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule controllersGoThroughPorts = noClasses()
        .that().resideInAPackage("..adapter.in.web..")
        .should().dependOnClassesThat()
        .resideInAPackage("..adapter.out.persistence..");
}
