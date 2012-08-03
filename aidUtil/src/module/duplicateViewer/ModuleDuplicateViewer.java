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

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import module.MaintenanceModule;
import util.LocationTag;

public class ModuleDuplicateViewer extends MaintenanceModule{
	JPanel displayArea = new JPanel();
	DuplicateListModel dlm = new DuplicateListModel();
	JList<DuplicateEntry> duplicateList = new JList<>(dlm);
	JScrollPane duplicateScrollPane = new JScrollPane(duplicateList);
	
	Container OptionPanel;
	HashMap<String, Path> tagMap = new HashMap<>();
	LinkedList<DuplicateGroup> groups = new LinkedList<>();
	boolean stop = false;
	
	AidDAO sql;
	
	ListSelectionListener selectionListener = new ListSelectionListener() {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = duplicateList.getSelectedIndex();
			if(index >= 0 && index <= dlm.getSize()-1 ){
				displayImage(dlm.get(index));
			}
		}
	};
	
	@Override
	public void optionPanel(Container container) {
		container.setLayout(new GridLayout(-1,2));
		container.add(duplicateScrollPane);
		container.add(displayArea);
		displayArea.setLayout(new GridLayout(0,1));
		
		this.OptionPanel = container;
		
	}

	@Override
	public void start() {
		stop = false;
		duplicateList.removeAll();
		
		sql = new AidDAO(getConnectionPool());
		duplicateList.removeListSelectionListener(selectionListener);
		
		discoverTags();
		
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
			
			// only one tag in list is a valid state
			if(tags.size() != 1){
				continue;
			}
			
			tagMap.put(tags.get(0), f.toPath());
		}
	}
	
	private void displayImage(DuplicateEntry dupe){
		
		try {
			displayArea.removeAll(); // clear the panel
			
			// Create an image input stream on the image
		    ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(dupe.getPath()));

		    // Find all image readers that recognize the image format
		    Iterator iter = ImageIO.getImageReaders(iis);
		    if (!iter.hasNext()) {
		    	displayArea.add(new JLabel("Unable to display image"));
		        return;
		    }

		    // Use the first reader
		    ImageReader reader = (ImageReader)iter.next();

		    ImageReadParam params = reader.getDefaultReadParam();
		    reader.setInput(iis, true, true);
		    
		    int xSampleRate =  (int)Math.ceil((double) reader.getHeight(0) / (double)displayArea.getHeight());
		    int ySampleRate = (int)Math.ceil((double) reader.getWidth(0) / (double)displayArea.getWidth());

		    int sampleRate = Math.max(xSampleRate, ySampleRate);
		    
		    if(sampleRate < 1){
		    	sampleRate = 1;
		    }
		    
		    params.setSourceSubsampling(sampleRate, sampleRate, 0, 0);
		    
			Image img = reader.read(0, params);
			
			displayArea.add(new JLabel(new ImageIcon(img),JLabel.CENTER));
			
		} catch (IOException e) {
			displayArea.add(new JLabel("Failed to read image"));
		}
		
		displayArea.revalidate();
		displayArea.repaint();
	}
	
	private void deleteMarked(DuplicateGroup group){
		for(DuplicateEntry de : group.getEntries()){
			if(de.isSelected()){
				try {
					Files.delete(de.getPath());
					sql.deleteDuplicateByPath(de.getPath());
					sql.deleteIndexByPath(de.getPath());
					group.removeEntry(de);
				} catch (IOException e) {
					error("Failed to delete " + de.getPath().toString());
				}
			}
		}
		
		// if the group only has one entry left, it is no longer needed
		if(group.getSize() <= 1){
			for(DuplicateEntry de : group.getEntries()){
				// only remove, but do NOT delete
				sql.deleteDuplicateByPath(de.getPath());
				sql.deleteIndexByPath(de.getPath());
				group.removeEntry(de);
			}
			
			groups.remove(this);
		}
	}
	
	private void loadDuplicates(){
		setStatus("Loading duplicates...");
		info("Loading duplicates...");
		ArrayList<DuplicateEntry> dupeList = new ArrayList<>(sql.size(AidTables.Fileduplicate));
		
		
		setStatus("Sorting duplicates...");
		info("Sorting duplicates...");
		Collections.sort(dupeList);
		
		// id, dupeLoc, dupepath, origloc, origpath
		for(String[] s : sql.getDuplicates()){
			DuplicateEntry de;
			
			if(stop){
				break;
			}
			
			String hash = s[0];
			
			if(tagMap.get(s[1]) != null){
				de = new DuplicateEntry(hash, tagMap.get(s[1]).resolve(s[2])); // absolute path (tag found)
			} else {
				de = new DuplicateEntry(hash, Paths.get(s[2])); // relative path (tag not found)
			}
			
			dupeList.add(de);
		}
		
		DuplicateGroup dg = new DuplicateGroup();
		String hash = "";
		
		setStatus("Creating groups...");
		info("Creating groups...");
		
		for(DuplicateEntry de : dupeList){
			if(! de.getHash().equals(hash)){
				hash = de.getHash(); // set hash for next group
				
				// only add groups with more than one entry
				if(! (dg.getSize() <= 1)){
					groups.add(dg);
				}
				
				dg = new DuplicateGroup();
			}
			
			de.setGroup(dg);
			dg.addEntry(de);
		}
		
		setStatus("Adding duplicates to GUI...");
		info("Adding duplicates to GUI...");
		
		int duplicates = 0;
		
		for(DuplicateGroup d : groups){
			for(DuplicateEntry de : d.getEntries()){
				dlm.addElement(de);
				duplicates++;
			}
		}
		
		info(duplicates + " duplicates in " + groups.size() + " groups.");
		duplicateList.addListSelectionListener(selectionListener);
	}
	
	class DuplicateEntry implements Comparable<DuplicateEntry>{
		private static final long serialVersionUID = 1L;
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
	
	static class DuplicateGroup {
		static int groupRunningNumber = 0;
		
		int groupId;
		Color color;
		
		LinkedList<DuplicateEntry> entries = new LinkedList<>();

		public DuplicateGroup() {
			this.groupId = groupRunningNumber++;
			
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
		
		public int getSize(){
			return entries.size();
		}
		
		public LinkedList<DuplicateEntry> getEntries(){
			return entries;
		}

		public Color getColor() {
			return color;
		}
	}
	

	
	class DuplicateListModel extends DefaultListModel<DuplicateEntry>{
		private static final long serialVersionUID = 1L;
		
	}
}
