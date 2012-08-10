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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GroupFilter {
	public static void onlyVisibleTagGroups(List<DuplicateGroup> groups) {
		Iterator<DuplicateGroup> ite = groups.iterator();

		while (ite.hasNext()) {
			DuplicateGroup group = ite.next();
			LinkedList<Entry> entries = group.getEntries();

			if (hasRelativeEntries(entries)) {
				ite.remove();
			}
		}
	}

	public static void onlyFullyValidGroups(List<DuplicateGroup> groups) {
		Iterator<DuplicateGroup> ite = groups.iterator();

		while (ite.hasNext()) {
			DuplicateGroup group = ite.next();
			LinkedList<Entry> entries = group.getEntries();

			if (hasRelativeEntries(entries) || hasInvalidEntries(entries)) {
				ite.remove();
			}
		}
	}

	private static boolean hasInvalidEntries(List<Entry> entries) {
		if (entries.isEmpty()) {
			return false;
		}

		boolean hasInvalid = false;

		for (Entry entry : entries) {
			if (hasInvalid) {
				break;
			}
			Path entryPath = entry.getPath();
			hasInvalid = !Files.exists(entryPath);
		}

		return hasInvalid;
	}

	private static boolean hasRelativeEntries(List<Entry> entries) {
		if (entries.isEmpty()) {
			return false;
		}

		boolean hasRelative = false;

		for (Entry entry : entries) {
			if (hasRelative) {
				break;
			}
			Path entryPath = entry.getPath();
			hasRelative = !entryPath.isAbsolute();
		}

		return hasRelative;
	}
}
