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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.mockito.asm.Label;

import util.LocationTag;

public class ModuleDuplicateViewer extends MaintenanceModule{
	JPanel displayArea = new JPanel();
	JPanel duplicateList = new JPanel();
	JScrollPane duplicateScrollBar = new JScrollPane(duplicateList);
	Container OptionPanel;
	
	@Override
	public void optionPanel(Container container) {
		container.setLayout(new GridLayout(-1,2));
		container.add(duplicateScrollBar);
		container.add(displayArea);
		duplicateList.setLayout(new GridLayout(0,1));
		
		this.OptionPanel = container;
		
	}

	@Override
	public void start() {
		if(! new File(getPath()).exists()){
			error("Invalid path");
			return;
		}
		
		AidDAO sql = new AidDAO(getConnectionPool());
		for(String s : sql.getDuplicates(LocationTag.findTags(getPath()).get(0))){
			duplicateList.add(new DuplicateEntry(Paths.get(getPath()).getRoot().resolve(s)));
		}
		
		// call this to show components
		duplicateScrollBar.revalidate();
	}

	@Override
	public void Cancel() {
		duplicateList.removeAll();
		
		duplicateScrollBar.revalidate();
	}
	
	private void displayImage(Path path){
		
		try {
			displayArea.removeAll(); // clear the panel
			
			// Create an image input stream on the image
		    ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(path));

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
	
	class DuplicateEntry extends JPanel {
		private static final long serialVersionUID = 1L;
		JCheckBox selected;
		JLabel pathLable;
		Path path;
		
		public DuplicateEntry(Path path) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			selected  = new JCheckBox();
			pathLable = new JLabel(path.toString());
			this.path = path;
			
			pathLable.addMouseListener(new Mouse(this.path));
			pathLable.setPreferredSize(new Dimension(300, 20));
			
			this.add(selected);
			this.add(pathLable);
		}
	}
	
	class Mouse extends MouseAdapter{
		Path path;
		public Mouse(Path path){
			this.path = path;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			displayImage(path);
		}
	}
}
