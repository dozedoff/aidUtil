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

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ModuleDuplicateViewer extends MaintenanceModule {
	JPanel displayArea = new JPanel();
	JPanel duplicateList = new JPanel();
	JScrollPane duplicateScrollBar = new JScrollPane(duplicateList);
	
	@Override
	public void optionPanel(Container container) {
		container.setLayout(new GridLayout(-1,2));
		container.add(duplicateScrollBar);
		container.add(displayArea);
		
		// set to 1 column grid to make it look like a list
		duplicateList.setLayout(new GridLayout(-1,1));
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
	
	class DuplicateEntry extends JPanel {
		private static final long serialVersionUID = 1L;
		
	}
}
