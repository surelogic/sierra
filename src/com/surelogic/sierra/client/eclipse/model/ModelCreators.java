package com.surelogic.sierra.client.eclipse.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.surelogic.sierra.entity.Artifact;
import com.surelogic.sierra.entity.CompilationUnit;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sps.client.facade.SPSClient;

public class ModelCreators {

	private static Vector<CategoryHolder> categoryHolders;
	private static Vector<CategoryHolder> uninterestingFindingsByCategory;

	private static Vector<PackageHolder> packageHolders;
	private static Vector<PackageHolder> uninterestingFindingsByPackage;

	private static Vector<PriorityHolder> uninterestingFindingsByPriority;
	private static Vector<PriorityHolder> prioritizedFindings;

	private final static SPSClient spsClient;

	static {
		spsClient = SPSClient.getInstance();
	}

	private static void createFindingsPackageModel(String projectName) {

		Map<String, Map<String, Map<CompilationUnit, Collection<Artifact>>>> pathMap = spsClient
				.retrieveInterestingArtifacts(projectName);

		Set<String> paths = pathMap.keySet();
		Iterator<String> pathIterator = paths.iterator();

		packageHolders = new Vector<PackageHolder>();

		while (pathIterator.hasNext()) {

			String path = pathIterator.next();
			Map<String, Map<CompilationUnit, Collection<Artifact>>> packageMap = pathMap
					.get(path);

			Set<String> packageNames = packageMap.keySet();

			Iterator<String> packageIterator = packageNames.iterator();

			while (packageIterator.hasNext()) {

				String packageName = packageIterator.next();
				PackageHolder packageHolder = new PackageHolder(packageName);

				Vector<ClassHolder> classHolders = new Vector<ClassHolder>();

				Map<CompilationUnit, Collection<Artifact>> classMap = packageMap
						.get(packageName);
				Set<CompilationUnit> compilationUnits = classMap.keySet();
				Iterator<CompilationUnit> compilationUnitIterator = compilationUnits
						.iterator();

				while (compilationUnitIterator.hasNext()) {

					CompilationUnit compilationUnit = compilationUnitIterator
							.next();
					String className = compilationUnit.getClassName();

					ClassHolder classHolder = new ClassHolder(className);
					classHolder.setPackageName(packageName);

					Collection<Artifact> artifacts = classMap
							.get(compilationUnit);

					classHolder.setFindings(artifacts);

					classHolders.add(classHolder);

				}

				packageHolder.setClasses(classHolders);
				packageHolders.add(packageHolder);
			}

		}
	}

	private static void createUninterestingFindingsPackageModel(
			String projectName) {

		Map<String, Map<String, Map<CompilationUnit, Collection<Artifact>>>> pathMap = spsClient
				.retrieveUninterestingArtifacts(projectName);

		Set<String> paths = pathMap.keySet();
		Iterator<String> pathIterator = paths.iterator();

		uninterestingFindingsByPackage = new Vector<PackageHolder>();

		while (pathIterator.hasNext()) {

			String path = pathIterator.next();
			Map<String, Map<CompilationUnit, Collection<Artifact>>> packageMap = pathMap
					.get(path);

			Set<String> packageNames = packageMap.keySet();

			Iterator<String> packageIterator = packageNames.iterator();

			while (packageIterator.hasNext()) {

				String packageName = packageIterator.next();
				PackageHolder packageHolder = new PackageHolder(packageName);

				Vector<ClassHolder> classHolders = new Vector<ClassHolder>();

				Map<CompilationUnit, Collection<Artifact>> classMap = packageMap
						.get(packageName);
				Set<CompilationUnit> compilationUnits = classMap.keySet();
				Iterator<CompilationUnit> compilationUnitIterator = compilationUnits
						.iterator();

				while (compilationUnitIterator.hasNext()) {

					CompilationUnit compilationUnit = compilationUnitIterator
							.next();
					String className = compilationUnit.getClassName();

					ClassHolder classHolder = new ClassHolder(className);
					classHolder.setPackageName(packageName);

					Collection<Artifact> artifacts = classMap
							.get(compilationUnit);

					classHolder.setFindings(artifacts);

					classHolders.add(classHolder);

				}

				packageHolder.setClasses(classHolders);
				uninterestingFindingsByPackage.add(packageHolder);
			}

		}
	}

	private static void createCategoryModel(String projectName) {

		Map<String, Collection<Artifact>> categoryMap = spsClient
				.retrieveCategorizedArtifacts(projectName);

		Set<String> categories = categoryMap.keySet();

		categoryHolders = new Vector<CategoryHolder>();

		Iterator<String> categoriesIterator = categories.iterator();

		while (categoriesIterator.hasNext()) {

			CategoryHolder categoryHolder = new CategoryHolder();
			String categoryName = categoriesIterator.next();
			Collection<Artifact> artifacts = categoryMap.get(categoryName);

			categoryHolder.setCategory(categoryName);
			categoryHolder.setFindings(artifacts);

			categoryHolders.add(categoryHolder);

		}
	}

	private static void createUnintersetingFindingsCategoryModel(
			String projectName) {

		Map<String, Collection<Artifact>> categoryMap = spsClient
				.retrieveCategorizedUninterestingArtifacts(projectName);

		Set<String> categories = categoryMap.keySet();

		uninterestingFindingsByCategory = new Vector<CategoryHolder>();

		Iterator<String> categoriesIterator = categories.iterator();

		while (categoriesIterator.hasNext()) {

			CategoryHolder categoryHolder = new CategoryHolder();
			String categoryName = categoriesIterator.next();
			Collection<Artifact> artifacts = categoryMap.get(categoryName);

			categoryHolder.setCategory(categoryName);
			categoryHolder.setFindings(artifacts);

			uninterestingFindingsByCategory.add(categoryHolder);

		}
	}

	private static void createUnintersetingFindingsPriorityModel(
			String projectName) {

		Map<Priority, Collection<Artifact>> priorityMap = spsClient
				.retrievePrioritizedUninterestingArtifacts(projectName);

		Set<Priority> priorities = priorityMap.keySet();

		uninterestingFindingsByPriority = new Vector<PriorityHolder>();

		Iterator<Priority> prioritiesIterator = priorities.iterator();

		while (prioritiesIterator.hasNext()) {

			PriorityHolder priorityHolder = new PriorityHolder();
			Priority priority = prioritiesIterator.next();
			Collection<Artifact> artifacts = priorityMap.get(priority);

			priorityHolder.setPriority(priority);
			priorityHolder.setFindings(artifacts);

			uninterestingFindingsByPriority.add(priorityHolder);

		}
	}

	private static void createPriorityModel(String projectName) {

		Map<Priority, Collection<Artifact>> priorityMap = spsClient
				.retrievePrioritizedArtifacts(projectName);

		Set<Priority> priorities = priorityMap.keySet();

		prioritizedFindings = new Vector<PriorityHolder>();

		Iterator<Priority> prioritiesIterator = priorities.iterator();

		while (prioritiesIterator.hasNext()) {

			PriorityHolder priorityHolder = new PriorityHolder();
			Priority priority = prioritiesIterator.next();
			Collection<Artifact> artifacts = priorityMap.get(priority);

			priorityHolder.setPriority(priority);
			priorityHolder.setFindings(artifacts);

			prioritizedFindings.add(priorityHolder);

		}
	}

	// private static void createRunsByProjectModel() {
	// Map<String, Collection<Run>> runsMap = spsClient
	// .retrieveRunsByProject();
	//
	// Set<String> projects = runsMap.keySet();
	//
	// runsByProject = new Vector<ProjectHolder>();
	//
	// Iterator<String> projectsIterator = projects.iterator();
	//
	// while (projectsIterator.hasNext()) {
	//
	// ProjectHolder projectHolder = new ProjectHolder();
	// String projectName = projectsIterator.next();
	// Collection<Run> runs = runsMap.get(projectName);
	//
	// projectHolder.setProjectName(projectName);
	// projectHolder.setRuns(runs);
	//
	// runsByProject.add(projectHolder);
	//
	// }
	//
	// }

	public static void destroyModels() {
		packageHolders = null;
		categoryHolders = null;
		prioritizedFindings = null;
		uninterestingFindingsByCategory = null;
		uninterestingFindingsByPackage = null;
		uninterestingFindingsByPriority = null;
		// runsByProject = null;
	}

	public static Vector<PackageHolder> getPackageModel(String projectName) {
		createFindingsPackageModel(projectName);
		return packageHolders;
	}

	public static Vector<PackageHolder> getUninterestingPackageModel(
			String projectName) {
		createUninterestingFindingsPackageModel(projectName);
		return uninterestingFindingsByPackage;
	}

	public static Vector<CategoryHolder> getCategoryModel(String projectName) {
		createCategoryModel(projectName);
		return categoryHolders;
	}

	public static Vector<CategoryHolder> getUninterestingCategoryModel(
			String projectName) {
		createUnintersetingFindingsCategoryModel(projectName);
		return uninterestingFindingsByCategory;
	}

	public static Vector<PriorityHolder> getPrioritizedModel(String projectName) {
		createPriorityModel(projectName);
		return prioritizedFindings;
	}

	public static Vector<PriorityHolder> getUninterestingPrioritizedModel(
			String projectName) {
		createUnintersetingFindingsPriorityModel(projectName);
		return uninterestingFindingsByPriority;
	}

	// public static Vector<ProjectHolder> getRunsByProject() {
	// createRunsByProjectModel();
	// return runsByProject;
	// }
}
