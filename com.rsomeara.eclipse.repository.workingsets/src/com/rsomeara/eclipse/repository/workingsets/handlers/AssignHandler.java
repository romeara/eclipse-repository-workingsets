/* *****************************************************************************
 * Copyright (c) 2016 romeara@live.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    romeara@live.com - initial API and implementation and/or initial documentation
 *******************************************************************************/

package com.rsomeara.eclipse.repository.workingsets.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Represents the result of issuing a command to Eclipse to assign working sets
 * to project based on the Git repository they belong to
 * 
 * <p>
 * Based on implementation in
 * {@link org.eclipse.jdt.internal.ui.workingsets.ConfigureWorkingSetAssignementAction}
 * </p>
 * 
 * @author romeara
 * @since 0.1
 */
@SuppressWarnings("restriction")
public class AssignHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		// Must be true - if false, execution will only happen the first time
		// the button is clicked
		return true;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection currentSelection = (IStructuredSelection) selection;

			Collection<IProject> projects = getSelectedProjects(currentSelection);
			Map<IProject, String> repositories = getProjectRepositoryNames(projects);
			Map<String, IWorkingSet> workingSets = getAllWorkingSets();

			Map<String, IWorkingSet> relevantWorkingSets = createOrFindRelevantWorkingSets(repositories, workingSets);
			addToWorkingSets(repositories, relevantWorkingSets);

			activateWorkingSets(relevantWorkingSets.values());
		}

		return null;
	}

	/**
	 * Filters the selection to project elements
	 * 
	 * @param selection
	 *            The selection made in the view
	 * @return A collection of the selected elements which are projects
	 */
	private Collection<IProject> getSelectedProjects(IStructuredSelection selection) {
		Collection<IProject> projects = new ArrayList<>();

		if (selection != null) {
			for (Object selected : selection.toArray()) {
				if (selected instanceof IProject) {
					projects.add((IProject) selected);
				} else if (selected instanceof IJavaProject) {
					projects.add(((IJavaProject) selected).getProject());
				}
			}
		}

		return projects;
	}

	/**
	 * Looks up the name of the repository, if any, each project was imported
	 * from
	 * 
	 * @param projects
	 *            The projects to look up repository information for
	 * @return A mapping of projects to repository name. Projects without
	 *         repository information will not be present in the returned map
	 */
	private Map<IProject, String> getProjectRepositoryNames(Collection<IProject> projects) {
		Map<IProject, String> repositories = new HashMap<>();

		for (IProject project : projects) {
			RepositoryMapping repositoryMapping = RepositoryMapping.getMapping(project);

			if (repositoryMapping != null) {
				repositories.put(project, repositoryMapping.getRepository().getDirectory().getParentFile().getName());
			}
		}

		return repositories;
	}

	/**
	 * @return The current working sets within the workspace, mapped by the
	 *         label used
	 */
	private Map<String, IWorkingSet> getAllWorkingSets() {
		Map<String, IWorkingSet> workingSets = new HashMap<>();

		for (IWorkingSet workingSet : PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets()) {
			workingSets.put(workingSet.getLabel(), workingSet);
		}

		return workingSets;
	}

	/**
	 * Adds the provided working sets to the active (visible) working sets
	 * within workspace views
	 * 
	 * @param workingSets
	 *            The working sets to make visible
	 */
	private void activateWorkingSets(Collection<IWorkingSet> workingSets) {
		Collection<IWorkingSet> activeSets = Arrays.asList(getWorkingSetModel().getActiveWorkingSets());

		Collection<IWorkingSet> inactive = new ArrayList<>(workingSets);
		inactive.removeAll(activeSets);

		for (IWorkingSet set : inactive) {
			addToActiveWorkingSets(set);
		}
	}

	/**
	 * Looks up or creates a working set with a name corresponding to the Git
	 * repositories of the selected projects
	 * 
	 * @param projectToRepository
	 *            Mapping of selected projects to the name of the Git repository
	 *            they are assigned to
	 * @param existingSets
	 *            The working sets already available in the workspace
	 * @return A mapping of working set names to working set representations
	 */
	private Map<String, IWorkingSet> createOrFindRelevantWorkingSets(Map<IProject, String> projectToRepository,
			Map<String, IWorkingSet> existingSets) {
		Map<String, IWorkingSet> relevantWorkingSets = new HashMap<>();
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

		for (String repositoryName : projectToRepository.values()) {
			IWorkingSet workingSet = existingSets.get(repositoryName);

			if (workingSet == null) {
				workingSet = workingSetManager.createWorkingSet(repositoryName, new IAdaptable[] {});
				workingSet.setId(IWorkingSetIDs.RESOURCE);
				workingSetManager.addWorkingSet(workingSet);
				existingSets.put(repositoryName, workingSet);
			}

			relevantWorkingSets.put(workingSet.getLabel(), workingSet);

		}

		return relevantWorkingSets;
	}

	/**
	 * Adds projects to working sets based on repository name
	 * 
	 * @param projectToRepository
	 *            A mapping from a project to the name of the Git repository it
	 *            belongs to
	 * @param workingSets
	 *            A mapping of working set name to working set definition
	 */
	private void addToWorkingSets(Map<IProject, String> projectToRepository, Map<String, IWorkingSet> workingSets) {
		for (Entry<String, IWorkingSet> workingSetEntry : workingSets.entrySet()) {
			Collection<IAdaptable> elements = new ArrayList<>(Arrays.asList(workingSetEntry.getValue().getElements()));

			for (Entry<IProject, String> projectEntry : projectToRepository.entrySet()) {
				if (workingSetEntry.getKey() != null && workingSetEntry.getKey().equals(projectEntry.getValue())
						&& !elements.contains(projectEntry.getKey())) {
					elements.add(projectEntry.getKey());
				}
			}

			IAdaptable[] adapted = workingSetEntry.getValue()
					.adaptElements(elements.toArray(new IAdaptable[elements.size()]));
			workingSetEntry.getValue().setElements(adapted);
		}
	}

	/**
	 * Adds a working set to the currently active working sets (visible in
	 * workspace views)
	 */
	private void addToActiveWorkingSets(IWorkingSet workingSet) {
		WorkingSetModel model = getWorkingSetModel();

		if (model != null) {
			model.addWorkingSets(new Object[] { workingSet });
			model.addActiveWorkingSet(workingSet);
		}
	}

	/**
	 * @return The current representation of the working sets defined in the
	 *         workspace
	 */
	private WorkingSetModel getWorkingSetModel() {
		WorkingSetModel result = null;

		IWorkbenchPage page = JavaPlugin.getActivePage();

		if (page != null) {
			IWorkbenchPart activePart = page.getActivePart();

			if (activePart instanceof PackageExplorerPart) {
				result = ((PackageExplorerPart) activePart).getWorkingSetModel();
			}
		}

		return result;
	}

}
