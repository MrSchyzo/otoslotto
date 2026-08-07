package com.mrschyzo.hungarian.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.*;

@Disabled
class ArchTests {
    private static final String ROOT = "com.mrschyzo.hungarian";

    private static final String INTERFACES =
            ROOT + ".interfaces..";

    private static final String APPLICATION =
            ROOT + ".application.service..";

    private static final String DOMAIN =
            ROOT + ".domain..";

    private static final String INFRASTRUCTURE =
            ROOT + ".infrastructure..";

    private final JavaClasses importedClasses =
            new ClassFileImporter()
                    .withImportOption(
                            ImportOption.Predefined.DO_NOT_INCLUDE_TESTS
                    )
                    .importPackages(ROOT);

    @Test
    void interfaces_should_only_depend_on_interfaces_and_application_services() {
        noClasses()
                .that()
                .resideInAPackage(INTERFACES)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        DOMAIN,
                        INFRASTRUCTURE
                )
                .check(importedClasses);
    }

    @Test
    void application_services_should_only_depend_on_domain() {
        noClasses()
                .that()
                .resideInAPackage(APPLICATION)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        INFRASTRUCTURE,
                        INTERFACES
                )
                .check(importedClasses);
    }

    @Test
    void infrastructure_should_only_depend_on_domain() {
        noClasses()
                .that()
                .resideInAPackage(INFRASTRUCTURE)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        APPLICATION,
                        INTERFACES
                )
                .check(importedClasses);
    }

    @Test
    void domain_should_only_depend_on_domain() {
        noClasses()
                .that()
                .resideInAPackage(DOMAIN)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        APPLICATION,
                        INTERFACES,
                        INFRASTRUCTURE
                )
                .check(importedClasses);
    }

    @Test
    void only_app_should_exist_in_root_package() {
        classes()
                .that()
                .resideInAPackage(ROOT)
                .should()
                .haveSimpleName("App")
                .check(importedClasses);
    }

    @Test
    void classes_should_only_exist_in_allowed_packages() {
        classes()
                .should()
                .resideInAnyPackage(
                        ROOT,
                        INTERFACES,
                        APPLICATION,
                        DOMAIN,
                        INFRASTRUCTURE
                )
                .check(importedClasses);
    }


    /**
     * Prevent accidental dependency cycles.
     */
    @Test
    void architecture_should_be_free_of_cycles() {
        slices()
                .matching(ROOT + ".(*)..")
                .should()
                .beFreeOfCycles()
                .check(importedClasses);
    }
}
