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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dozedoff.commonj.file.FileUtil;
import com.github.dozedoff.commonj.file.TextFileReader;

public class Settings {
	private static Settings instance = null;
	private String[] moduleNames;
	private Properties dbProperties;
	private Path appPath7zip;
	
	private static final String modulelistFileName = "modulelist.txt";
	private static final String dbPropoertiesFilename = "db.properties";
	private static final String aidUtilSettingsFilename = "aidUtil.properties";
	
	private static final String APP_PATH_KEY = "7zipAppPath";
	
	Logger logger = LoggerFactory.getLogger(Settings.class);
	private Settings() {
		// Singleton
	}
	
	public static Settings getInstance() {
		if(instance == null){
			instance = new Settings();
		}
		
		return instance;
	}
	
	public void loadSettings() {
		loadModuleList();
		loadDbSettings();
		loadAidUtilSettings();
	}

	private void loadModuleList() {
		File moduleListFile = new File(FileUtil.WorkingDir(), modulelistFileName);
		logger.info("Loading module list from {}", moduleListFile);
		
		if(!moduleListFile.exists()){
			logger.error("Could not find module list {}", moduleListFile);
			moduleNames = new String[0];
		}
		
		String moduleList = "";
		try {
			moduleList = new TextFileReader().read(moduleListFile);
		} catch (IOException e) {
			logger.error("Failed to load {}", moduleListFile, e);
		}
		
		moduleNames = moduleList.split("(\n|\r\n)");
		logger.info("Found {} entries in module list", moduleNames.length);
	}
	
	private void loadDbSettings() {
		logger.info("Loading database settings...");
		File dbPropertiesFile = new File(FileUtil.WorkingDir(), dbPropoertiesFilename);
		dbProperties = loadPropertiesFromFile(dbPropertiesFile);
	}
	
	private void loadAidUtilSettings() {
		logger.info("Loading program settings...");
		File aidUtilPropertiesFile = new File(FileUtil.WorkingDir(), aidUtilSettingsFilename);
		Properties aidUtilProperties = loadPropertiesFromFile(aidUtilPropertiesFile);
		appPath7zip = Paths.get((String)aidUtilProperties.get(APP_PATH_KEY));
	}
	
	private Properties loadPropertiesFromFile(File filepath) {
		Properties properties = new Properties();
		try {
			if (filepath.exists()) {
				FileReader fr = new FileReader(filepath);
				properties.load(fr);
				fr.close();
				logger.info("Loaded property file {} with {} entries", filepath, properties.size());
			} else {
				logger.error("Could not find {}", filepath);
			}
		} catch (IOException e) {
			logger.error("Failed to load {}", filepath, e);
		}

		return properties;
	}

	public String[] getModuleNames() {
		return moduleNames;
	}

	public Properties getDbProperties() {
		return dbProperties;
	}

	public Path getAppPath7zip() {
		return appPath7zip;
	}
}
