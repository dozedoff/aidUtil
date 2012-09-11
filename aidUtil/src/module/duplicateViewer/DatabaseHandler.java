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

import io.AidDAO;
import io.AidTables;
import io.ConnectionPool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DatabaseHandler {
	private final AidDAO sql;
	private final HashMap<String, Path> tagMap;
	
	public DatabaseHandler(ConnectionPool connPool, HashMap<String, Path> tagMap) {
		this.sql = new AidDAO(connPool);
		this.tagMap = tagMap;
	}
	
	public LinkedList<Entry> getDuplicates() {
		LinkedList<String[]> data = sql.getDuplicates();
		LinkedList<Entry> duplicates = processDuplicateData(data);
		return duplicates;
	}
	
	private LinkedList<Entry> processDuplicateData(List<String[]> data){
		LinkedList<Entry> entries = new LinkedList<>();
		
		for(String[] s : data){
			Entry entry;
			
			String hash = s[0];
			String location = s[1];
			String relvativeDuplicatePath = s[2];
			Path entryPath, rootPath;
			
			rootPath = getLocationPath(location);
			
			if(rootPath != null){
				entryPath = rootPath.resolve(relvativeDuplicatePath);
			} else {
				entryPath = Paths.get(relvativeDuplicatePath);
			}
			
			entry = new Entry(hash, entryPath);
			entries.add(entry);
		}
		
		return entries;
	}
	
	private Path getLocationPath(String location) {
		return tagMap.get(location);
	}
	
	public void deleteFromDuplicates(Entry entry) {
		sql.deleteDuplicateByPath(entry.getPath());
	}
}
