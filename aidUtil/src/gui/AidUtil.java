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
package gui;

import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import module.MaintenanceModule;

import app.Core;

public class AidUtil extends JFrame implements ActionListener{
	private static final long serialVersionUID = -3377472963066131706L;

	Core core;
	
	JTextField targetPath;
	JTextArea logArea;
	JPanel optionPanel, controlPanel;
	JButton start, cancel;
	JMenuBar mBar;
	JMenu moduleMenu;
	HashMap<JMenuItem, MaintenanceModule> guiModelMap = new HashMap<>();
	
	public AidUtil(Core core, List<MaintenanceModule> modules){
		this.core = core;
		init(modules);
	}
	
	private void init(List<MaintenanceModule> modules){
		// set up JFrame
		setSize(700, 500);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// add menubar
		JMenuBar mBar = new JMenuBar();
		setJMenuBar(mBar);
		mBar.add(moduleMenu = new JMenu("Module"));
		
		// add modules to menu
		for(MaintenanceModule mm : modules){
			JMenuItem jmi = new JMenuItem(mm.getClass().getSimpleName());
			jmi.addActionListener(this);
			moduleMenu.add(jmi);
			guiModelMap.put(jmi, mm);
		}
		
		// create JPanel for contorls
		controlPanel = new JPanel();
		controlPanel.add(targetPath = new JTextField(30));
		targetPath.setToolTipText("Folder to process");
		
		// add components to main window
		add(controlPanel, BorderLayout.NORTH);
		add(optionPanel = new JPanel(), BorderLayout.CENTER);
		add(new JScrollPane( logArea = new JTextArea(10,30) ),BorderLayout.SOUTH);
	}
	
	private void setActiveModule(final MaintenanceModule module){
		optionPanel.removeAll(); // clear the option panel
		
		// add module specific options
		module.optionPanel(optionPanel);
		module.setLog(logArea);
		
		// add start/cancel buttons
		JButton start = new JButton("Start");
		JButton cancel = new JButton("Cancel");
		
		// assign module methods to buttons
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				module.start();
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				module.Cancel();
			}
		});
		
		optionPanel.add(start);
		optionPanel.add(cancel);
		optionPanel.validate();
		this.validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// map Menuitem to module
		if(e.getSource() instanceof JMenuItem){
			setActiveModule(guiModelMap.get(e.getSource()));
		}
	}
}