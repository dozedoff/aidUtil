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
package duplicateViewer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import module.duplicateViewer.DuplicateGroup;
import module.duplicateViewer.Entry;
import module.duplicateViewer.GroupFilter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import archiveIndexer.ArchiveUnpackerTest;

public class GroupFilterTest {
	LinkedList<DuplicateGroup> groups;
	static Path validFilePath;
	
	DuplicateGroup allValid, someRelative, someInvalid;
	
	@BeforeClass
	public static void beforeClassSetup() throws Exception {
		validFilePath = Paths.get(ArchiveUnpackerTest.class.getResource("test.7z").toURI());
	}
	
	@Before
	public void setUp() throws Exception {
		createGroups();
	}

	@Test
	public void testOnlyVisibleTagGroups() {
		GroupFilter.onlyVisibleTagGroups(groups);
		
		assertThat(groups, hasItems(allValid, someInvalid));
		assertThat(groups.size(), is(2));
	}

	@Test
	public void testOnlyFullyValidGroups() {
		GroupFilter.onlyFullyValidGroups(groups);
		
		assertThat(groups, hasItems(allValid));
		assertThat(groups.size(), is(1));
	}
	
	private void createGroups() {
		groups = new LinkedList<>();
		
		allValid = new DuplicateGroup("1");
		allValid.addEntry(new Entry("1", validFilePath));
		
		someRelative = new DuplicateGroup("2");
		someRelative.addEntry(new Entry("2", validFilePath));
		someRelative.addEntry(new Entry("2", Paths.get("relative/")));
		
		someInvalid = new DuplicateGroup("3");
		someInvalid.addEntry(new Entry("3", Paths.get("C:\\non-existant\\foo.txt")));
		
		groups.add(allValid);
		groups.add(someRelative);
		groups.add(someInvalid);
	}
}
