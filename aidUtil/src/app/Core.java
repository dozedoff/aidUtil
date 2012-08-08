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
import file.FileWalker;
import gui.AidUtil;
import io.BoneConnectionPool;
import io.ConnectionPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
			connPool = new BoneConnectionPool(dbProps,5);
			connPool.startPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			aidUtil = new AidUtil(this, loadModules(), connPool);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		aidUtil.setVisible(true);
	}
	
	/**
	 * This method creates an instance of all maintenance modules in the bin/module directory
	 * @return a list of instances of all maintenance modules
	 * @throws IOException 
	 */
	public List<MaintenanceModule> loadModules() throws IOException{
		String relativePath = "bin";
		Path moduleDir = Paths.get(FileUtil.WorkingDir().toString(),relativePath);
		LinkedList<Path> moduleFiles = FileWalker.walkFileTreeWithFilter(moduleDir, new ModuleFilter());
		
		LinkedList<MaintenanceModule> modules = new LinkedList<>();

		for(Path file : moduleFiles){
				try {
					String qualifiedName = createQualifiedName(moduleDir, file);
					modules.add(ModuleFactory.createInstance(qualifiedName));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return modules;
	}
	
	private String createQualifiedName(Path basePath, Path filepath) {
		Path relativePath = basePath.relativize(filepath);
		String qualifiedName = relativePath.toString();
		qualifiedName = qualifiedName.replace("\\", ".");
		qualifiedName = qualifiedName.replace(".class", "");
		return qualifiedName;
	}
}