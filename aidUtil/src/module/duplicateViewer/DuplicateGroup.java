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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class DuplicateGroup {
	static int groupRunningNumber = 0;
	
	int groupId;
	Color color;
	String imageHashValue;
	Path imagePath = null;
	
	LinkedList<Entry> entries = new LinkedList<>();

	/**
	 * Use DuplicateGroup(String) instead.
	 * Groups need a hash value to correctly add duplicates.
	 */
	@Deprecated
	public DuplicateGroup() {
		this.groupId = groupRunningNumber++;
		imageHashValue = null;
		setColor();
	}
	
	public DuplicateGroup(String imageHashValue){
		this.groupId = groupRunningNumber++;
		this.imageHashValue = imageHashValue;
	}
	
	private void setColor() {
		if(groupId % 2 == 0){
			color = Color.black;
		}else{
			color = Color.blue;
		}
	}
	
	public void addEntry(Entry de){
		entries.add(de);
	}
	
	public void removeEntry(Entry de){
		entries.remove(de);
	}
	
	public int getSize() {
		return entries.size();
	}
	
	public LinkedList<Entry> getEntries() {
		return entries;
	}
	
	public LinkedList<Entry> getSelected() {
		LinkedList<Entry> selected = new LinkedList<>();

		for (Entry entry : entries) {
			if (entry.isSelected()) {
				selected.add(entry);
			}
		}

		return selected;
	}
	
	public LinkedList<Entry> getNotSelected() {
		LinkedList<Entry> selected = new LinkedList<>();

		for (Entry entry : entries) {
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
		
		for(Entry entry : entries){
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
	
	public String getImageHashValue() {
		return imageHashValue;
	}
	
	public Path getImagepath() {
		if((imagePath == null) || (! Files.exists(imagePath))) {
			imagePath = searchForImagePath();
		}
		
		return imagePath;
	}
	
	public boolean allEntriesAbsolute() {
		if(isEmpty()){
			return false;
		}
		
		boolean allAbsolute = true;
		
		for(Entry entry : entries) {
			if(! allAbsolute){
				break;
			}
			
			Path entryPath = entry.getPath();
			allAbsolute = allAbsolute && entryPath.isAbsolute();
		}
		
		return allAbsolute;
	}
	
	public boolean allEntriesExist() {
		if(isEmpty()) {
			return false;
		}
		
		boolean allExist = true;
		
		for(Entry entry : entries) {
			if(! allExist) {
				break;
			}
			
			Path entryPath = entry.getPath();
			allExist = allExist && Files.exists(entryPath);
		}
		
		return allExist;
	}

	private Path searchForImagePath() {
		Path imgPath = Paths.get("empty");
		
		if(! entries.isEmpty()) {
			imgPath = entries.getFirst().getPath();
			
			for(Entry entry : entries) {
				Path path = entry.getPath();
				if(Files.exists(path)) {
					imgPath = path;
					break;
				}
			}
		}
		
		return imgPath;
	}
	
	public static void resetRunningNumber() {
		groupRunningNumber = 0;
	}
	
	public void selectAll(boolean select) {
		for(Entry entry : entries){
			entry.setSelected(select);
		}
	}

	@Override
	public boolean equals(Object o) {
		if(o == null){
			return false;
		}
		
		if(! (o instanceof DuplicateGroup)){
			return false;
		}

		DuplicateGroup toCompare = (DuplicateGroup) o;
		
		if(this.imageHashValue.equalsIgnoreCase(toCompare.getImageHashValue())){
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return String.valueOf(groupId);
	}
}