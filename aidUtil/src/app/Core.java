/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'AidUtil', a collection of maintenance tools for 'Aid'.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package app;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import module.MaintenanceModule;
import module.ModuleDnwCleanUp;
import module.ModuleFactory;

import file.FileUtil;
import gui.AidUtil;

public class Core {
	AidUtil aidUtil;
	
	public static void main(String args[]){
		new Core().startCore();
	}
	
	public void startCore(){
		// TODO create and start connection pool
		
		aidUtil = new AidUtil(this,loadModules());
		aidUtil.setVisible(true);
	}
	
	/**
	 * This method creates an instance of all maintenance modules in the bin/module directory
	 * @return a list of instances of all maintenance modules
	 */
	public List<MaintenanceModule> loadModules(){
		String relativePath = "bin"+File.separator+"module"; // path to the bin/module directory
		File moduleDir = new File(FileUtil.WorkingDir(),relativePath);
		String [] files = moduleDir.list();	// get list of files in the directory
		
		LinkedList<MaintenanceModule> modules = new LinkedList<>();

		for(String s : files){
			// filter out files that are not maintenance modules
			if(s.startsWith("Module") && s.endsWith(".class") && !s.equals(ModuleFactory.class.getSimpleName()+".class") && !s.contains("$")){
				try {
					s = "module."+s; // otherwise there will be ClassNotFound error
					modules.add(ModuleFactory.createModule(s.replace(".class", ""))); // create instance of the module
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return modules;
	}
}