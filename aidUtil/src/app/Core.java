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

import file.FileUtil;
import gui.AidUtil;
import io.BoneConnectionPool;
import io.ConnectionPool;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import module.MaintenanceModule;
import module.ModuleFactory;

public class Core {
	AidUtil aidUtil;
	ConnectionPool connPool;
	
	public static void main(String args[]){
		new Core().startCore();
	}
	
	public void startCore(){
		
		try {
			// load database properties
			Properties dbProps = new Properties();
			dbProps.load(Core.class.getResourceAsStream("db.properties"));
			
			// create connection pool
			connPool = new BoneConnectionPool(dbProps,10);
			connPool.startPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		aidUtil = new AidUtil(this, loadModules(), connPool);
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