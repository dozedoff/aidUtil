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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import module.duplicateViewer.Entry;
import module.duplicateViewer.DuplicateGroup;
import module.duplicateViewer.Selector;

import org.junit.Before;
import org.junit.Test;

public class SelectorTest {
	private DuplicateGroup dupeGroup;
	
	private static final int ENTRIES_PER_GROUP = 5;
	
	@Before
	public void setup() throws IOException {
		createGroup();
	}
	
	private void createGroup() throws IOException {
		dupeGroup = new DuplicateGroup();
		
		for(int i=0; i<ENTRIES_PER_GROUP; i++ ){
			Path entryPath = Files.createTempFile("DuplicateSelectorTest", null);
			dupeGroup.addEntry(new Entry(String.valueOf(i), entryPath, i));
		}
	}
	
	private void createGroupWithSelected(int numOfSelected) throws IOException {
		dupeGroup = new DuplicateGroup();
		
		for(int i=0; i<numOfSelected; i++ ){
			Path entryPath = Files.createTempFile("DuplicateSelectorTest", null);
			Entry entry = new Entry(String.valueOf(i), entryPath, i);
			entry.setSelected(true);
			dupeGroup.addEntry(entry);
		}
		
		for(int i=numOfSelected; i<ENTRIES_PER_GROUP; i++ ){
			Path entryPath = Files.createTempFile("DuplicateSelectorTest", null);
			dupeGroup.addEntry(new Entry(String.valueOf(i), entryPath, i));
		}
	}

	@Test
	public void testClearAllSelections() throws IOException {
		createGroupWithSelected(2);
		assertThat(dupeGroup.getSelected().size(), is(2));
		
		Selector.clearAllSelections(dupeGroup);
		
		assertThat(dupeGroup.getSelected().size(), is(0));
		assertThat(dupeGroup.getNotSelected().size(), is(ENTRIES_PER_GROUP));
	}
	
	@Test
	public void testSelectAll() throws IOException {
		createGroupWithSelected(2);
		assertThat(dupeGroup.getSelected().size(), is(2));
		
		Selector.selectAllEntries(dupeGroup);
		
		assertThat(dupeGroup.getSelected().size(), is(ENTRIES_PER_GROUP));
		assertThat(dupeGroup.getNotSelected().size(), is(0));
	}

	@Test
	public void testSelectAllButOldest() {
		Selector.selectAllButOldest(dupeGroup);

		assertThat(dupeGroup.getSelected().size(), is(ENTRIES_PER_GROUP-1));
		assertThat(dupeGroup.getNotSelected().size(), is(1));
		assertThat(dupeGroup.getNotSelected().getFirst().getLastModified(), is(0L));
	}

	@Test
	public void testSelectAllButNewest() {
		Selector.selectAllButNewest(dupeGroup);
		
		assertThat(dupeGroup.getSelected().size(), is(ENTRIES_PER_GROUP-1));
		assertThat(dupeGroup.getNotSelected().size(), is(1));
		assertThat(dupeGroup.getNotSelected().getFirst().getLastModified(), is(ENTRIES_PER_GROUP-1L));
	}

	@Test
	public void testSelectAllFromPath() throws IOException {
		Path tempDirectory = Files.createTempDirectory("DuplicateSelectorTest");
		LinkedList<Path> files = createFilesAndStructure(tempDirectory);
		createGroupFromFiles(files);
		
		LinkedList<DuplicateGroup> groupList = new LinkedList<>();
		groupList.add(dupeGroup);
		
		Selector.selectAllFromPath(groupList, tempDirectory.resolve("dirA/"));
		
		assertThat(dupeGroup.getSelected().size(), is(3));
		
		for(Entry entry : dupeGroup.getSelected()) {
			Path entryPath = entry.getPath();
			String pathString = entryPath.toString();
			assertThat(pathString.toLowerCase().contains("dirb"), is(false));
		}
	}
	
	private void createGroupFromFiles(LinkedList<Path> files) throws IOException {
		dupeGroup = new DuplicateGroup();
		
		for(Path file : files){
			Entry entry = new Entry("1", file, 0);
			dupeGroup.addEntry(entry);
		}
		
	}
	
	private LinkedList<Path> createFilesAndStructure(Path tempDirectory) throws IOException {
		LinkedList<Path> files = new LinkedList<>();
		
		files.add(tempDirectory.resolve("dirA/0.txt"));
		files.add(tempDirectory.resolve("dirA/dir1/a.txt"));
		files.add(tempDirectory.resolve("dirA/dir2/b.txt"));
		
		files.add(tempDirectory.resolve("dirB/c.txt"));
		files.add(tempDirectory.resolve("dirB/dir3/d.txt"));
		
		for(Path file : files){
			Path directory = file.getParent();
			Files.createDirectories(directory);
			Files.createFile(file);
		}
		
		return files;
	}
	


}
