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

class DuplicateEntry implements Comparable<DuplicateEntry>{
	boolean selected = false;
	Path path;
	DuplicateGroup group;
	
	String hash;
	
	public DuplicateEntry(String hash, Path path) {
		this.path = path;
		this.hash = hash;
	}

	public Path getPath() {
		return path;
	}
	
	public String getHash() {
		return hash;
	}

	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	public DuplicateGroup getGroup() {
		return group;
	}

	public void setGroup(DuplicateGroup group) {
		this.group = group;
	}

	@Override
	public int compareTo(DuplicateEntry o) {
		return this.hash.compareTo(o.getHash());
	}
	
	@Override
	public String toString() {
		return path.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof DuplicateEntry)){
			return false;
		}
		
		return ((DuplicateEntry)obj).getHash().equals(getHash());
	}
}