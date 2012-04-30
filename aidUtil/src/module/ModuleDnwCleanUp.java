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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.sun.media.sound.ModelSource;

public class ModuleDnwCleanUp extends MaintenanceModule implements ActionListener {
Thread worker;
ButtonGroup group;
JRadioButton delete,report,move; // radio buttons for selecting work mode
JTextField movePath; // path where files should be moved to
enum Mode {Delete, Report, Move, None};

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
		if(worker != null){
			worker.interrupt();
			worker = null;
		}
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
	
	private void makeOptionButtons(Mode selected,  Container container){
		ButtonGroup bg = new ButtonGroup();
		
		for(Mode s : Mode.values()){
			JRadioButton rb = new JRadioButton(s.toString());
			bg.add(rb);
			container.add(rb);
			rb.addActionListener(new ModeSetter(selected, s));
		}
	}
	
	class ModeSetter implements ActionListener{
		Mode selected, toSet;
		
		public ModeSetter(Mode selected, Mode toSet) {
			this.selected = selected;
			this.toSet = toSet;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			selected = toSet;
		}
	}
	
	public void cleanUp(){
		// validate settings
		if(getPath() == null || !new File(getPath()).exists()){
			return;
		}
		
	}
	
	/**
	 * This thread walks the specified directory and sub-directories and adds the found
	 * files to the queue.
	 */
	class FileWorker extends Thread{
		LinkedBlockingQueue<File> fileQueue;
		
		public FileWorker(){
			super(ModuleDnwCleanUp.class.getSimpleName()+" FileWalker");
		}
		
		@Override
		public void run() {
			fileQueue = new LinkedBlockingQueue<>();
			
			// TODO Add filewalker here
		}
	}
	
	/**
	 * This thread calculates the hash values for the files and adds file and hash to the queue.
	 */
	class HashWorker extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * This thread compares the hash values against the database. 
	 */
	class DBworker extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * This thread performs the selected action.
	 */
	class CleanUpWorker extends Thread{
		
	}
}
