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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.swing.DefaultListModel;

public class Deleter {
	DatabaseHandler dbHandler;
	
	public Deleter(DatabaseHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	public void deleteAllSelected(DefaultListModel<DuplicateGroup> groupModel) throws IOException {
		int size = groupModel.getSize();

		for (int i = 0; i < size; i++) {
			DuplicateGroup group = groupModel.get(i);
			deleteSelected(group);
		}
	}

	public void deleteSelected(DuplicateGroup group) throws IOException {
		LinkedList<Entry> entries = group.getSelected();

		for (Entry entry : entries) {
			deleteEntryFromDisk(entry);
			deleteEntryFromGroup(group, entry);
		}
	}

	private void deleteEntryFromDisk(Entry entry) throws IOException {
		Path entryPath = entry.getPath();
		Files.delete(entryPath);
		dbHandler.deleteFromDuplicates(entry);
	}

	private void deleteEntryFromGroup(DuplicateGroup group, Entry entry) {
		group.removeEntry(entry);
	}
}
