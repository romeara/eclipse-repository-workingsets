package com.rsomeara.eclipse.repository.workingsets.expression;

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

//TODO romeara doc
public class ProjectPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean result = false;

		if (receiver instanceof Collection<?>) {
			for (Object obj : ((Collection<?>) receiver)) {
				result = (obj instanceof IProject) || (obj instanceof IJavaProject);

				if (result) {
					break;
				}
			}
		}

		return result;
	}

}
