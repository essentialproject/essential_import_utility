/**
* Copyright (c)2009 - 2014 Enterprise Architecture Solutions ltd.
* This file is part of Essential Architecture Manager, 
* the Essential Architecture Meta Model and The Essential Project.
*
* Essential Architecture Manager is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Essential Architecture Manager is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Essential Architecture Manager.  If not, see <http://www.gnu.org/licenses/>.
* 
 */
package com.enterprise_architecture.essential.importutility.integration;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.storage.clips.ClipsFilesExportProjectPlugin;

/**
 * @author jasonpowell
 *
 */
public class EIUClipsFilesExportProjectPlugin extends ClipsFilesExportProjectPlugin {
	

	public EIUClipsFilesExportProjectPlugin() {
		super();
	}
	
	
	protected boolean isCompatibleProject(Project project) {
		return true;
	}
	
	
}
