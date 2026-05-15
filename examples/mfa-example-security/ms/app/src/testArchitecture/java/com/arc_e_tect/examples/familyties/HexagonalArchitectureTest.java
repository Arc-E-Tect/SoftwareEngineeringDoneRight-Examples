package com.arc_e_tect.examples.familyties;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "com.arc_e_tect.examples.familyties";

    // Import all classes including transitive dependencies to check for indirect violations
    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

    /**
     * Domain layer must only depend on Java core libraries and application core packages.
     * This rule checks ALL dependencies transitively - if domain depends on ports,
     * and ports depend on a framework, this test will fail.
     */
    @Test
    void domainShouldOnlyDependOnJavaUtilAndDomainClasses() {
        classes()
                .that().resideInAPackage("..application.domain..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java.util..", "java.lang..", "..application.domain..", "..application.port..", "..application.common..")
                .because("Domain must only depend on Java core libraries (java.util, java.lang), domain classes, ports (interfaces), and common exceptions - including transitive dependencies")
                .check(classes);
    }

    /**
     * Ports must be interfaces and only depend on Java core libraries and domain.
     * This prevents any framework dependencies from leaking into domain through ports.
     */
    @Test
    void portsShouldBeInterfacesAndOnlyDependOnJavaUtilAndDomain() {
        classes()
                .that().resideInAPackage("..application.port..")
                .should().beInterfaces()
                .because("Ports define contracts only")
                .check(classes);

        classes()
                .that().resideInAPackage("..application.port..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java.util..", "java.lang..", "..application.domain..", "..application.port..")
                .because("Ports must only depend on Java core libraries and domain classes - no frameworks allowed to prevent transitive pollution")
                .check(classes);
    }

    @Test
    void coreShouldNotDependOnAdapters() {
        noClasses()
                .that().resideOutsideOfPackage("..adapters..")
                .should().dependOnClassesThat().resideInAnyPackage("..adapters..")
                .because("Adapters are outer layers and must not be referenced by domain or ports")
                .check(classes);
    }

    /**
     * Common utilities must stay pure Java to prevent framework dependencies
     * from leaking into domain through common exceptions or utilities.
     */
    @Test
    void commonShouldOnlyDependOnJavaUtil() {
        classes()
                .that().resideInAPackage("..application.common..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java.util..", "java.lang..", "..application.common..")
                .because("Common utilities must only depend on Java core libraries - no frameworks to prevent transitive pollution")
                .check(classes);
    }

    /**
     * Additional safeguard: explicitly verify no Spring, Jakarta, or Lombok
     * dependencies exist anywhere in the core application layer (domain, ports, common).
     * This catches transitive dependencies that might slip through.
     * 
     * Note: Lombok annotations have @Retention(SOURCE) and are removed during compilation,
     * so they cannot be detected by ArchUnit's bytecode analysis. Use CheckStyle or PMD
     * with import rules if you need to enforce Lombok-free source code.
     */
    @Test
    void coreApplicationLayerShouldHaveNoFrameworkDependencies() {
        noClasses()
                .that().resideInAnyPackage("..application.domain..", "..application.port..", "..application.common..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "javax.persistence..",
                    "javax.validation..",
                    "org.hibernate..",
                    "lombok..",
                    "com.fasterxml.jackson.."
                )
                .because("Core application layer must be completely free of framework dependencies, including transitive ones")
                .check(classes);
    }
}
