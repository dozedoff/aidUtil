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

import java.awt.Color;
import java.util.LinkedList;

public class DuplicateGroup {
	static int groupRunningNumber = 0;
	
	int groupId;
	Color color;
	
	LinkedList<DuplicateEntry> entries = new LinkedList<>();

	public DuplicateGroup() {
		this.groupId = groupRunningNumber++;
		setColor();
	}
	
	private void setColor() {
		if(groupId % 2 == 0){
			color = Color.black;
		}else{
			color = Color.blue;
		}
	}
	
	public void addEntry(DuplicateEntry de){
		entries.add(de);
	}
	
	public void removeEntry(DuplicateEntry de){
		entries.remove(de);
	}
	
	public int getSize() {
		return entries.size();
	}
	
	/**
	 * Exposes structure, not good.
	 */
	@Deprecated
	public LinkedList<DuplicateEntry> getEntries() {
		return entries;
	}
	
	public LinkedList<DuplicateEntry> getSelected() {
		LinkedList<DuplicateEntry> selected = new LinkedList<>();

		for (DuplicateEntry entry : entries) {
			if (entry.isSelected()) {
				selected.add(entry);
			}
		}

		return selected;
	}
	
	public LinkedList<DuplicateEntry> getNotSelected() {
		LinkedList<DuplicateEntry> selected = new LinkedList<>();

		for (DuplicateEntry entry : entries) {
			if (!entry.isSelected()) {
				selected.add(entry);
			}
		}

		return selected;
	}

	/**
	 * No longer needed.
	 */
	@Deprecated
	public Color getColor() {
		return color;
	}

	public int getGroupId() {
		return groupId;
	}
	
	public boolean areAllSelected() {
		boolean allSelected = true;
		
		if(isEmpty()) {
			return false;
		}
		
		for(DuplicateEntry entry : entries){
			allSelected = allSelected && entry.isSelected();
		}
		
		return allSelected;
	}
	
	public boolean hasOnlyOneEntry() {
		return entries.size() == 1;
	}
	
	public boolean isEmpty() {
		return entries.isEmpty();
	}
}