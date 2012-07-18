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
package module;

import io.AidDAO;
import io.AidTables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.LocationTag;

public class ModuleDuplicateViewer extends MaintenanceModule{
	JPanel displayArea = new JPanel();
	JPanel duplicateList = new JPanel();
	JScrollPane duplicateScrollBar = new JScrollPane(duplicateList);
	Container OptionPanel;
	HashMap<String, Path> tagMap = new HashMap<>();
	
	boolean stop = false;
	
	AidDAO sql;
	
	@Override
	public void optionPanel(Container container) {
		container.setLayout(new GridLayout(-1,2));
		container.add(duplicateScrollBar);
		container.add(displayArea);
		duplicateList.setLayout(new GridLayout(0,1));
		displayArea.setLayout(new GridLayout(0,1));
		
		// Solution from http://stackoverflow.com/a/11398879/891292
		duplicateScrollBar.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
	        @Override
	        public void adjustmentValueChanged(AdjustmentEvent e) {
	            // The user scrolled the List (using the bar, mouse wheel or something else):
	            if (e.getAdjustmentType() == AdjustmentEvent.TRACK){
	                // Jump to the next "block" (which is a row".
	                e.getAdjustable().setBlockIncrement(100);
	            }
	        }
	    });
		
		this.OptionPanel = container;
		
	}

	@Override
	public void start() {
		stop = false;
		duplicateList.removeAll();
		
		sql = new AidDAO(getConnectionPool());
		discoverTags();
		
		setStatus("Loading duplicates...");
		info("Loading duplicates...");
		ArrayList<DuplicateEntry> dupeList = new ArrayList<>(sql.size(AidTables.Fileduplicate));
		
		// id, dupeLoc, dupepath, origloc, origpath
		for(String[] s : sql.getDuplicates()){
			if(stop){
				break;
			}
			
			if(tagMap.get(s[1]) != null){
				dupeList.add(new DuplicateEntry(s));
			}
		}
		
		setStatus("Sorting duplicates...");
		info("Sorting duplicates...");
		Collections.sort(dupeList);
		
		setStatus("Adding duplicates to GUI...");
		info("Adding duplicates to GUI...");
		
		Thread guiLoader = new GuiLoader(dupeList);
		guiLoader.start();
		
		try {guiLoader.join();} catch (InterruptedException e) {}
		
		// call this to show components
		duplicateScrollBar.revalidate();
		
		setStatus("Ready");
	}

	@Override
	public void Cancel() {
		stop = true;
		deleteMarked();
		duplicateList.removeAll();
		
		duplicateScrollBar.revalidate();
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
	
	private void deleteMarked(){
		Component[] components = duplicateList.getComponents();
		
		for(Component c : components){
			if(c instanceof DuplicateEntry){
				DuplicateEntry d = (DuplicateEntry)c;
				
				if(d.isSelected()){
					try {
						Files.delete(d.getPath());
						sql.deleteDuplicateByPath(d.getPath());
						sql.deleteIndexByPath(d.getPath());
					} catch (IOException e) {
						error("Failed to delete " + d.getPath().toString());
					}
				}
			}
		}
	}
	
	class DuplicateEntry extends JPanel implements Comparable<DuplicateEntry>{
		private static final long serialVersionUID = 1L;
		JCheckBox selected;
		JLabel pathLable;
		Path path;
		
		String hash;
		
		public DuplicateEntry(String [] data) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			//  0   1    2
			// id, loc, path
			
			path = tagMap.get(data[1]).resolve(data[2]);
			this.hash = data[0];
			
			selected  = new JCheckBox();
			pathLable = new JLabel(path.toString());
			pathLable.setToolTipText(path.toString());
			
			pathLable.addMouseListener(new Mouse(this));
			pathLable.setPreferredSize(new Dimension(300, 20));
			
			this.add(selected);
			this.add(pathLable);
		}

		public Path getPath() {
			return path;
		}
		
		public String getHash() {
			return hash;
		}

		public boolean isSelected(){
			return selected.isSelected();
		}
		
		public void setColor(Color color){
			pathLable.setForeground(color);
		}

		@Override
		public int compareTo(DuplicateEntry o) {
			return this.hash.compareTo(o.getHash());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null || !(obj instanceof DuplicateEntry)){
				return false;
			}
			
			return ((DuplicateEntry)obj).getHash().equals(getHash());
		}
	}
	
	class Mouse extends MouseAdapter{
		DuplicateEntry dupe;
		
		public Mouse(DuplicateEntry dupe){
			this.dupe = dupe;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			displayImage(dupe);
		}
	}
	
	class GuiLoader extends Thread {
		ArrayList<DuplicateEntry> list;
		int counter = 0;
		final int LOAD_LIMIT = 3000; // to prevent the GUI from freezing
		
		public GuiLoader(ArrayList<DuplicateEntry> list) {
			this.list = list;
		}
		
		@Override
		public void run() {
			String hash = "";
			boolean color = true;
			
			for(DuplicateEntry d : list){
				if(stop){
					break;
				}
				
				if(! d.getHash().equals(hash)){
					hash = d.getHash();
					color = !color;
				}
				
				if(color){
					d.setColor(Color.blue);
				}
				
				counter ++;
				duplicateList.add(d);
				
				if(counter >= 3000){
					info("Load limit of " + LOAD_LIMIT + " reached");
					break;
				}
			}
		}
	}
}