# Eclipse Repository-Based Workingset Plug-in

Plug-in for the Eclipse IDE platform which allows 1-click sorting of projects into working sets based on the source repository

# Installation

The Repository Working Set plug-in is hosted via Eclipse update site at the address:

`https://raw.githubusercontent.com/romeara/eclipse-repository-workingsets/master/com.rsomeara.eclipse.repository.workingsets.site`

# Use

The plug-in adds a context menu to the Java package explorer window - simply right-click on any selection of projects and find the entry:

[alt text](/docs/PopupMenu.png)

Clicking the entry will take all selected projects and add them to a working set named the same as the repository the project with imported from. Working sets will be created if they do not already exist, and projects not imported from a known source control system will not be moved

# Contribution

Contributions are always welcome! 

* An Eclipse plug-in development environment is required to work with the projects - the last used Eclipse version was Mars 2. 
* The plug-ins are currently developed against Java 7
* All pull requests should be done against the master branch

## Licensing

The repository working set plug in is licensed under the Eclipse Public License 1.0, as specified and documented in the LICENSE.md file within each plug-in
