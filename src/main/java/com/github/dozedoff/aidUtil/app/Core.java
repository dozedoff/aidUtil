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
package com.github.dozedoff.aidUtil.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.github.dozedoff.aidUtil.gui.AidUtil;
import com.github.dozedoff.aidUtil.module.MaintenanceModule;
import com.github.dozedoff.aidUtil.module.ModuleFactory;
import com.github.dozedoff.commonj.file.FileUtil;
import com.github.dozedoff.commonj.file.FileWalker;
import com.github.dozedoff.commonj.file.TextFileReader;
import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.github.dozedoff.commonj.io.ConnectionPool;

public class Core {
	AidUtil aidUtil;
	ConnectionPool connPool;
	Logger logger = LoggerFactory.getLogger(Core.class);
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
			logger.error("Failed to connect to database", e);
		}
		
		try {
			aidUtil = new AidUtil(this, loadModules(), connPool);
		} catch (Exception e) {
			logger.error("Failed to start AidUtil", e);
			System.exit(1);
		}
		
		aidUtil.setVisible(true);
	}
	
	/**
	 * This method creates an instance of all maintenance modules in the bin/module directory
	 * @return a list of instances of all maintenance modules
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<MaintenanceModule> loadModules() throws IOException, URISyntaxException{
		LinkedList<MaintenanceModule> modules = new LinkedList<>();
		
		String modulelistFileName = "modulelist.txt";
		File moduleListFile = new File(FileUtil.WorkingDir(), modulelistFileName);
		logger.info("Loading module list from {}", moduleListFile);
		
		if(!moduleListFile.exists()){
			logger.error("Could not find module list {}", moduleListFile);
			return modules;
		}
		
		String moduleList = new TextFileReader().read(moduleListFile);
		String[] moduleEntries = moduleList.split("(\n|\r\n)");
		logger.info("Found {} entries in module list", moduleEntries.length);
		
		for(String fullyqualifiedName : moduleEntries){
				try {
					logger.info("Loading module {}", fullyqualifiedName);
					modules.add(ModuleFactory.createInstance(fullyqualifiedName));
				} catch (Exception e) {
					logger.error("Failed to load module {}", fullyqualifiedName, e);
				}
		}
		
		logger.info("Modules loaded");
		return modules;
	}
}