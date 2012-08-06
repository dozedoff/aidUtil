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
package module.duplicateViewer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DuplicateSelector {
	public static void clearAllSelections(DuplicateGroup group) {
		LinkedList<DuplicateEntry> selected = group.getSelected();
		
		for(DuplicateEntry entry : selected) {
			entry.setSelected(false);
		}
	}
	
	public static void selectAllEntries(DuplicateGroup group) {
		LinkedList<DuplicateEntry> notSelected = group.getNotSelected();
		
		for(DuplicateEntry entry : notSelected) {
			entry.setSelected(true);
		}
	}
	
	public static void selectAllButOldest(DuplicateGroup group) {
		selectAllEntries(group);
		
		LinkedList<DuplicateEntry> entries = group.getSelected();
		sortByAscendingDate(entries);
		DuplicateEntry oldest = entries.getFirst();
		
		oldest.setSelected(false);
	}
	
	public static void selectAllButNewest(DuplicateGroup group) {
		selectAllEntries(group);
		
		LinkedList<DuplicateEntry> entries = group.getSelected();
		sortByAscendingDate(entries);
		DuplicateEntry newest = entries.getLast();
		
		newest.setSelected(false);
	}
	
	private static void sortByAscendingDate(LinkedList<DuplicateEntry> entries) {
		Collections.sort(entries, new DateComparator());
	}
	
	public static void selectAllFromPath(List<DuplicateGroup> groupList, Path path) {
		for(DuplicateGroup group : groupList) {
			markEntriesInPath(group,path);
		}
	}
	
	private static void markEntriesInPath(DuplicateGroup group, Path path) {
		clearAllSelections(group);
		
		for(DuplicateEntry entry : group.getNotSelected()) {
			if(isInPath(entry, path)) {
				entry.setSelected(true);
			}
		}
	}
	
	private static boolean isInPath(DuplicateEntry entry, Path path) {
		Path entryPath = entry.getPath();
		return entryPath.startsWith(path);
	}
}
