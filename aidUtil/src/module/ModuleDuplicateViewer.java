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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.mockito.asm.Label;

public class ModuleDuplicateViewer extends MaintenanceModule {
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
		
		//DEBUG
		for(int i=0; i<30; i++ ){
			duplicateList.add(new DuplicateEntry(String.valueOf(i)));
		}
		
		duplicateScrollBar.revalidate();
	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub
		
	}
	
	class DuplicateEntry extends JPanel {
		private static final long serialVersionUID = 1L;
		JCheckBox selected;
		
		public DuplicateEntry(String path) {
			selected  = new JCheckBox(path);
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.add(selected);
		}
	}
}
