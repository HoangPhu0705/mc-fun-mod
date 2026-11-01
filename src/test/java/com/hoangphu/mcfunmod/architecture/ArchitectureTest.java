package com.hoangphu.mcfunmod.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Executable version of docs/ARCHITECTURE.md. If a rule fails, fix the dependency;
 * only change a rule together with the doc.
 */
@AnalyzeClasses(packages = "com.hoangphu.mcfunmod", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
	private static final String ROOT = "com.hoangphu.mcfunmod";

	@ArchTest
	static final ArchRule coreIsPureJava = noClasses()
			.that().resideInAPackage(ROOT + ".core..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"net.minecraft..", "com.mojang..", "net.fabricmc..", "org.spongepowered..")
			.because("core must stay plain Java so it can be unit tested without Minecraft");

	@ArchTest
	static final ArchRule coreDependsOnlyOnCore = noClasses()
			.that().resideInAPackage(ROOT + ".core..")
			.should().dependOnClassesThat().resideOutsideOfPackages(ROOT + ".core..", "java..")
			.because("dependencies point inward: outer layers call core, never the other way");

	@ArchTest
	static final ArchRule modelIsInnermost = noClasses()
			.that().resideInAPackage(ROOT + ".core.model..")
			.should().dependOnClassesThat().resideInAnyPackage(ROOT + ".core.service..", ROOT + ".core.port..")
			.because("the model is the innermost ring");

	@ArchTest
	static final ArchRule portsAreImplementedByAdapters = classes()
			.that().implement(resideInAPackage(ROOT + ".core.port.."))
			.should().resideInAPackage(ROOT + ".adapter..")
			.because("Minecraft-backed implementations of ports belong in adapter");

	@ArchTest
	static final ArchRule mixinsAreNotReferenced = noClasses()
			.that().resideOutsideOfPackages(ROOT + ".mixin..", ROOT + ".client.mixin..")
			.should().dependOnClassesThat().resideInAnyPackage(ROOT + ".mixin..", ROOT + ".client.mixin..")
			.because("mixin classes do not exist at runtime; referencing them crashes the game");

	@ArchTest
	static final ArchRule registryOnlyDeclaresContent = noClasses()
			.that().resideInAPackage(ROOT + ".registry..")
			.should().dependOnClassesThat().resideInAnyPackage(
					ROOT + ".event..", ROOT + ".command..", ROOT + ".network..", ROOT + ".adapter..")
			.because("registries only declare content");

	@ArchTest
	static final ArchRule noPackageCycles = slices()
			.matching(ROOT + ".(*)..")
			.should().beFreeOfCycles();
}
