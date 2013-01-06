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
package com.github.dozedoff.aidUtil.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class LocationTag {
	public final static String LOCATION_TAG_PREFIX = "LOCATIONTAG-";
	
	public static LinkedList<String> findTags(Path path){
		LinkedList<String> tags = new LinkedList<>();
		
		if(path == null || path.getRoot() == null){
			return tags;
		}
		
		Path currentSearchPath = path;
		
		while(currentSearchPath != currentSearchPath.getRoot()){
			File[] dirFiles = currentSearchPath.toFile().listFiles();
			tags = searchListForTags(dirFiles);
			if(!tags.isEmpty()){
				break;
			}
			
			currentSearchPath = currentSearchPath.getParent();
		}
		
		return tags;
	}
	
	private static LinkedList<String> searchListForTags(File[] filesToSearch) {
		LinkedList<String> tagList = new LinkedList<>();
		if (filesToSearch == null) {
			return tagList;
		}

		for (File file : filesToSearch) {
			if (file.getName().startsWith(LOCATION_TAG_PREFIX)) {
				tagList.add(file.getName().substring(LOCATION_TAG_PREFIX.length()));
			}
		}

		return tagList;
	}
	
	public static LinkedList<String> findTags(String path){
		Path ppath = Paths.get(path);
		return findTags(ppath);
	}
}
