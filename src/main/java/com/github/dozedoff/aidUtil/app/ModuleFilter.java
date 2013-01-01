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
import java.io.FileFilter;

import com.github.dozedoff.aidUtil.module.ModuleFactory;

public class ModuleFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		if(! file.isFile()){
			return false;
		}
		
		String filename = file.getName();
		return isValidModule(filename);
	}
	
	private boolean isValidModule(String filename) {
		return isModuleClass(filename) && (! isFactoryClass(filename)) && (! isFilterClass(filename)) && (! isInnerClass(filename));
	}
	
	private boolean isFactoryClass(String filename) {
		return filename.equals(ModuleFactory.class.getSimpleName()+".class");
	}
	
	private boolean isInnerClass(String filename) {
		return filename.contains("$");
	}
	
	private boolean isModuleClass(String filename) {
		return filename.startsWith("Module") && filename.endsWith(".class");
	}
	
	private boolean isFilterClass(String filename) {
		return filename.equals(ModuleFilter.class.getSimpleName()+".class");
	}
}
