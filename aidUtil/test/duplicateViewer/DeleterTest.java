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
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.swing.DefaultListModel;

import module.duplicateViewer.DatabaseHandler;
import module.duplicateViewer.Deleter;
import module.duplicateViewer.Entry;
import module.duplicateViewer.DuplicateGroup;

import org.junit.Before;
import org.junit.Test;

public class DeleterTest {
	LinkedList<Path> files;
	static final int GROUP_SIZE = 5;
	DuplicateGroup duplicateGroup;
	DatabaseHandler dbHandler = mock(DatabaseHandler.class);
	Deleter deleter;
	
	@Before
	public void setup() throws IOException {
		files = new LinkedList<>();
		duplicateGroup = createDuplicateGroup(GROUP_SIZE, false);
		deleter = new Deleter(dbHandler);
	}
	
	@Test
	public void testDeleteSelectedNoneMarked() throws IOException {
		sanityCheck();
		deleter.deleteSelected(duplicateGroup);
		
		for(Path file : files){
			assertThat(Files.exists(file), is(true));
		}
		
		assertThat(duplicateGroup.getSize(), is(GROUP_SIZE));
	}
	
	@Test
	public void testDeleteSelectedAllMarked() throws IOException {
		duplicateGroup = createDuplicateGroup(GROUP_SIZE, true);
		sanityCheck();
		deleter.deleteSelected(duplicateGroup);
		
		for(Path file : files){
			assertThat(Files.exists(file), is(false));
		}
		
		assertThat(duplicateGroup.isEmpty(), is(true));
	}
	
	private DuplicateGroup createDuplicateGroup(int groupSize, boolean entriesAreSelected) throws IOException {
		files.clear();
		DuplicateGroup duplicateGroup = new DuplicateGroup("1");
		
		for(int i=0; i < groupSize; i++){
			duplicateGroup.addEntry(createDuplicateEntry(entriesAreSelected));
		}
		
		return duplicateGroup;
	}

	@Test
	public void testDeleteSelectedOneIsMarked() throws IOException {
		sanityCheck();
		
		Entry entryToDelete = createDuplicateEntry(true);
		entryToDelete.setSelected(true);
		duplicateGroup.addEntry(entryToDelete);
		files.remove(entryToDelete.getPath());
		
		Path fileToDelete = entryToDelete.getPath();
		
		deleter.deleteSelected(duplicateGroup);
		
		for(Path file : files){
			assertThat(Files.exists(file), is(true));
		}
		
		assertThat(Files.exists(fileToDelete), is(false));
		assertThat(duplicateGroup.getSelected().size(), is(0));
		assertThat(duplicateGroup.getSize(), is(GROUP_SIZE));
		
	}
	
	@Test
	public void testDeleteAllSelected() throws IOException {
		DefaultListModel<DuplicateGroup> model = new DefaultListModel<>();
		duplicateGroup = createDuplicateGroup(GROUP_SIZE, true);
		sanityCheck();

		model.addElement(duplicateGroup);
		deleter.deleteAllSelected(model);

		for (Path file : files) {
			assertThat(Files.exists(file), is(false));
		}

		assertThat(duplicateGroup.isEmpty(), is(true));
	}
	
	private void sanityCheck() {
		assertThat(files.size(), is(GROUP_SIZE));
		for(Path file : files){
			assertThat(Files.exists(file), is(true));
		}
	}
	
	private Entry createDuplicateEntry(boolean entryIsSelected) throws IOException {
		Path testFile = Files.createTempFile("DuplicateDeleterTest", ".txt");
		files.add(testFile);
		Entry entry = new Entry("12345", testFile);
		entry.setSelected(entryIsSelected);
		return entry;
	}
}
