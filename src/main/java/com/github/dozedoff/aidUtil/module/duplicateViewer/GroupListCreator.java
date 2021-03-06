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
package com.github.dozedoff.aidUtil.module.duplicateViewer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class GroupListCreator {
	public static LinkedList<DuplicateGroup> createList(List<Entry> entries) {
		LinkedHashMap<String, DuplicateGroup> groupMap = populateMap(entries);
		LinkedList<DuplicateGroup> groupList = convertMapToList(groupMap);
		return groupList;
	}
	
	private static LinkedHashMap<String, DuplicateGroup> populateMap(List<Entry> entries) {
		LinkedHashMap<String, DuplicateGroup> groupMap = new LinkedHashMap<>();

		for(Entry entry : entries) {
			addEntryToMap(groupMap, entry);
		}
		return groupMap;
	}
	
	private static void addEntryToMap(Map<String, DuplicateGroup> groupMap, Entry entry) {
		String entryHash = entry.getHash();
		
		if(groupMap.containsKey(entryHash)){
			DuplicateGroup existingGroup = groupMap.get(entryHash);
			existingGroup.addEntry(entry);
		}else{
			DuplicateGroup newGroup = new DuplicateGroup(entryHash);
			newGroup.addEntry(entry);
			groupMap.put(entryHash, newGroup);
		}
	}
	
	private static LinkedList<DuplicateGroup> convertMapToList(LinkedHashMap<String, DuplicateGroup> groupMap) {
		LinkedList<DuplicateGroup> groupList = new LinkedList<>(groupMap.values());
		return groupList;
	}
}
