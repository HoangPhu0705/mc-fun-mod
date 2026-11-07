package com.codefee.mcfunmod.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Executable version of docs/ARCHITECTURE.md. If a rule fails, fix the dependency;
 * only change a rule together with the doc.
 */
@AnalyzeClasses(packages = "com.codefee.mcfunmod", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
	private static final String ROOT = "com.codefee.mcfunmod";
	private static final String FEATURE = ROOT + ".feature";
	private static final String CLIENT_FEATURE = ROOT + ".client.feature";

	@ArchTest
	static final ArchRule logicIsPureJava = noClasses()
			.that().resideInAnyPackage(ROOT + "..logic..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"net.minecraft..", "com.mojang..", "net.fabricmc..", "org.spongepowered..")
			.because("logic must stay plain Java so it can be unit tested without Minecraft");

	@ArchTest
	static final ArchRule logicDependsOnlyOnLogic = noClasses()
			.that().resideInAnyPackage(ROOT + "..logic..")
			.should().dependOnClassesThat().resideOutsideOfPackages(ROOT + "..logic..", "java..")
			.because("logic is called by the Minecraft side, never the other way around");

	@ArchTest
	static final ArchRule featuresOnlyUseEachOthersEntryClass = classes()
			.that().resideInAnyPackage(FEATURE + "..", CLIENT_FEATURE + "..")
			.should(onlyUseOtherFeaturesThroughTheirEntryClass())
			.because("features must stay independent; shared code belongs in common");

	@ArchTest
	static final ArchRule noCyclesBetweenFeatures = slices()
			.matching(FEATURE + ".(*)..")
			.should().beFreeOfCycles();

	@ArchTest
	static final ArchRule commonDoesNotKnowFeatures = noClasses()
			.that().resideInAPackage(ROOT + ".common..")
			.should().dependOnClassesThat().resideInAnyPackage(FEATURE + "..", CLIENT_FEATURE + "..")
			.because("common is shared by every feature, so it cannot depend on any of them");

	@ArchTest
	static final ArchRule mixinsAreNotReferenced = noClasses()
			.that().resideOutsideOfPackages(ROOT + ".mixin..", ROOT + ".client.mixin..")
			.should().dependOnClassesThat().resideInAnyPackage(ROOT + ".mixin..", ROOT + ".client.mixin..")
			.because("mixin classes do not exist at runtime; referencing them crashes the game");

	@ArchTest
	static final ArchRule noTopLevelCycles = slices()
			.matching(ROOT + ".(*)..")
			.should().beFreeOfCycles();

	/**
	 * A class in feature A may depend on classes in feature B only if they sit directly in B's root
	 * package (its entry class), e.g. {@code feature.luckycoin.LuckyCoin}, never {@code feature.luckycoin.item.*}.
	 * Client and server parts of the same feature count as one feature.
	 */
	private static ArchCondition<JavaClass> onlyUseOtherFeaturesThroughTheirEntryClass() {
		return new ArchCondition<>("only use other features through their entry class") {
			@Override
			public void check(JavaClass origin, ConditionEvents events) {
				String originFeature = featureOf(origin.getPackageName());
				for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
					String targetPackage = dependency.getTargetClass().getPackageName();
					String targetFeature = featureOf(targetPackage);
					boolean otherFeature = targetFeature != null && !targetFeature.equals(originFeature);
					boolean entryClass = targetPackage.equals(FEATURE + "." + targetFeature)
							|| targetPackage.equals(CLIENT_FEATURE + "." + targetFeature);
					if (otherFeature && !entryClass) {
						events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
					}
				}
			}
		};
	}

	/** Feature name for a package inside feature/ or client/feature/, otherwise null. */
	private static String featureOf(String packageName) {
		for (String prefix : new String[] {FEATURE + ".", CLIENT_FEATURE + "."}) {
			if (packageName.startsWith(prefix)) {
				String rest = packageName.substring(prefix.length());
				int dot = rest.indexOf('.');
				return dot < 0 ? rest : rest.substring(0, dot);
			}
		}
		return null;
	}
}
