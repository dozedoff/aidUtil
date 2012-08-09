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

import image.SubsamplingImageLoader;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import module.MaintenanceModule;
import net.miginfocom.swing.MigLayout;
import util.LocationTag;

public class ModuleDuplicateViewer extends MaintenanceModule{
	JPanel displayArea, duplicateViewOptions;
	DefaultListModel<Entry> elm = new DefaultListModel<>();
	DefaultListModel<DuplicateGroup> glm = new DefaultListModel<>();
	
	JList<DuplicateGroup> groupList;
	JList<Entry> entryList;
	JScrollPane entryScrollPane, groupScrollPane;
	
	HashMap<String, Path> tagMap = new HashMap<>();
	LinkedList<DuplicateGroup> groups = new LinkedList<>();
	boolean stop = false;
	
	DatabaseHandler dbHandler;
	
	ListSelectionListener groupSelectionListener = new ListSelectionListener() {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = groupList.getSelectedIndex();
			if(isValidRange(index)){
				DuplicateGroup group = glm.get(index);
				populateEntryList(group);
				
				Path imagepath = group.getImagepath();
				displayImage(imagepath);
			}
		}
		
		private boolean isValidRange(int index) {
			return (index >= 0 && index <= glm.getSize()-1 );
		}
	};
	
	private void populateEntryList(DuplicateGroup group) {
		elm.removeAllElements();
		
		LinkedList<Entry> allEntries = new LinkedList<>();
		allEntries.addAll(group.getSelected());
		allEntries.addAll(group.getNotSelected());
		
		for(Entry entry : allEntries) {
			elm.addElement(entry);
		}
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void optionPanel(Container container) {
		duplicateViewOptions = new JPanel();
		displayArea = new JPanel();
		
		groupList = new JList<>(glm);
		entryList = new JList<>(elm);
		entryScrollPane = new JScrollPane(entryList);
		groupScrollPane = new JScrollPane(groupList);
		
		groupList.addListSelectionListener(groupSelectionListener);
		
		duplicateViewOptions.setLayout(new MigLayout("", "[132.00][][grow]", "[grow]"));
		displayArea.setLayout(new MigLayout("fill"));

		duplicateViewOptions.add(groupScrollPane, "growy");
		duplicateViewOptions.add(entryScrollPane, "growy");
		duplicateViewOptions.add(displayArea, "grow");
		
		container.add(duplicateViewOptions, "grow");
	}

	@Override
	public void start() {
		stop = false;
		glm.removeAllElements();
		elm.removeAllElements();
		
		discoverTags();
		dbHandler = new DatabaseHandler(getConnectionPool(), tagMap);
		loadDuplicates();
		setStatus("Ready");
	}

	@Override
	public void Cancel() {
		stop = true;
	}
	
	private void discoverTags(){
		File[] roots = File.listRoots();
		
		for(File f : roots){
			LinkedList<String> tags = LocationTag.findTags(f.toPath());
			
			if(! isValidTagList(tags)){
				continue;
			}
			
			tagMap.put(tags.get(0), f.toPath());
		}
	}
	
	private boolean isValidTagList(List<String> tags) {
		int size = tags.size();
		if(size == 1) {
			return true;
		}else{
			return false;
		}
	}
	
	private void displayImage(Path imagepath){
		
		try {
			displayArea.removeAll();
			JLabel imageLabel = SubsamplingImageLoader.loadImage(imagepath, displayArea.getSize());
			displayArea.add(imageLabel);
		} catch (IOException e) {
			unableToDisplayImage("Error accessing image");
		} catch (Exception ex) {
			unableToDisplayImage(ex.getMessage());
		}
		
		displayArea.revalidate();
		displayArea.repaint();
	}

	private void unableToDisplayImage(String message) {
		displayArea.add(new JLabel(message));
	}

	private void loadDuplicates(){
		setStatus("Loading duplicates...");
		info("Loading duplicates...");
		
		LinkedList<Entry> entries = dbHandler.getDuplicates();
		
		setStatus("Creating groups...");
		info("Creating groups...");
		
		LinkedList<DuplicateGroup> groups = GroupListCreator.createList(entries);
		
		setStatus("Adding groups to GUI...");
		info("Adding groups to GUI...");
		
		SwingUtilities.invokeLater(new GroupListPopulator(groups));
		
		int duplicateNum = entries.size();
		int groupNum = groups.size();
		
		info(duplicateNum + " duplicates in " + groupNum + " groups.");
	}
	
	class GroupListPopulator implements Runnable {
		List<DuplicateGroup> groups;
		
		public GroupListPopulator(List<DuplicateGroup> groups) {
			this.groups = groups;
		}

		@Override
		public void run() {
			for (DuplicateGroup group : groups) {
				glm.addElement(group);
			}
		}
	}
}
