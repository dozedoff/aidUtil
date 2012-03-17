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

import io.ConnectionPool;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ModuleDnwCleanUp implements MaintenanceModule, ActionListener {
JTextArea logArea;
Thread worker;
ConnectionPool cPool;
ButtonGroup group;
JRadioButton delete,report,move; // radio buttons for selecting work mode
JTextField movePath; // path where files should be moved to

	@Override
	public void optionPanel(Container container) {
		// create buttons
		delete = new JRadioButton("Delete");
		report = new JRadioButton("Report");
		move = new JRadioButton("Move");
		
		// group buttons
		group = new ButtonGroup();
		group.add(delete);
		group.add(report);
		group.add(move);
		
		// create separate panel and add buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(delete);
		buttonPanel.add(report);
		buttonPanel.add(move);
		
		// add action listeners
		delete.addActionListener(this);
		report.addActionListener(this);
		move.addActionListener(this);
		
		// set up text field for target folder
		movePath = new JTextField(20);
		movePath.setEnabled(false);
		movePath.setToolTipText("Path where files should be moved to");
		
		// add all components to the option panel
		container.add(movePath);
		container.add(buttonPanel);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void Cancel() {
		worker.interrupt();
	}

	@Override
	public void setLog(JTextArea logArea) {
		this.logArea = logArea;
	}

	@Override
	public void setConnectionPool(ConnectionPool pool) {
		this.cPool = pool;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// textfield is only used in move mode
		if(e.getSource() == move){
			movePath.setEnabled(true);
		}else{
			movePath.setEnabled(false);
		}
	}
}
