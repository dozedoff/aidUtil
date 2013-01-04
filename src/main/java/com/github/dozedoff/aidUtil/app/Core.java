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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.github.dozedoff.aidUtil.gui.AidUtil;
import com.github.dozedoff.aidUtil.module.MaintenanceModule;
import com.github.dozedoff.aidUtil.module.ModuleFactory;
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
		Settings.getInstance().loadSettings();
		
		try {
			Properties dbProps = Settings.getInstance().getDbProperties();
			
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
		
		String[] moduleEntries = Settings.getInstance().getModuleNames();
		
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