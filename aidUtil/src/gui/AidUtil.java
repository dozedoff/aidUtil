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

import io.ConnectionPool;

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
import javax.swing.text.DefaultCaret;

import module.MaintenanceModule;
import net.miginfocom.swing.MigLayout;
import app.Core;

public class AidUtil extends JFrame implements ActionListener{
	private static final long serialVersionUID = -3377472963066131706L;

	Core core;
	ConnectionPool connPool;
	
	JTextField targetPath, status;
	JTextArea logArea;
	JPanel optionPanel;
	JButton start, cancel, clear;
	JMenuBar mBar;
	JMenu moduleMenu;
	HashMap<JMenuItem, MaintenanceModule> guiModelMap = new HashMap<>();
	
	public AidUtil(Core core, List<MaintenanceModule> modules, ConnectionPool connPool){
		this.core = core;
		this.connPool = connPool;
		
		init(modules);
	}
	
	private void init(List<MaintenanceModule> modules){
		// set up JFrame
		setSize(820, 530);
		setTitle("AidUtil");
		MigLayout migLayout = new MigLayout("debug", "[408.00][252.00][][][]", "[][][]");
		getContentPane().setLayout(migLayout);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// add menubar
		JMenuBar mBar = new JMenuBar();
		setJMenuBar(mBar);
		mBar.add(moduleMenu = new JMenu("Module"));
		
		// add modules to menu
		for(MaintenanceModule mm : modules){
			JMenuItem jmi = new JMenuItem(mm.getModuleName());
			jmi.addActionListener(this);
			moduleMenu.add(jmi);
			guiModelMap.put(jmi, mm);
		}
		
		targetPath = new JTextField();
		targetPath.setToolTipText("Folder to process");
		
		start = new JButton("Start");
		cancel = new JButton("Cancel");
		clear = new JButton("Clear");

		optionPanel = new JPanel();
		optionPanel.setLayout(new MigLayout());

		// set clear tooltip
		clear.setToolTipText("Clear the log area");
		logArea = new JTextArea(10,70);
		JScrollPane logScroll = new JScrollPane(logArea);
		
		getContentPane().add(targetPath, "cell 0 0,growx");
		status = new JTextField();
		status.setEditable(false);
		getContentPane().add(status, "cell 1 0,growx");
		getContentPane().add(start, "cell 2 0");
		getContentPane().add(cancel, "cell 3 0");
		getContentPane().add(clear, "cell 4 0");
		getContentPane().add(optionPanel, "cell 0 1 5 1,push ,grow");
		getContentPane().add(logScroll, "cell 0 2 5 1,growx");
		
		
		
		
		// enable auto-scroll for the TextArea
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// set action listener for cancel (done here as the action listener will not change)
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
			}
		});
	}
	
	private void setActiveModule(final MaintenanceModule module){
		optionPanel.removeAll();
		optionPanel.setLayout(new MigLayout());
		
		module.setConnectionPool(connPool);
		
		// add module specific options
		module.optionPanel(optionPanel);
		module.setLog(logArea);
		
		// set TextFields
		module.setPathField(targetPath);
		module.setStatusField(status);
		
		// change window title
		this.setTitle("AidUtil - " + module.getModuleName());
		
		// remove previous acction listeners
		for(ActionListener al : start.getActionListeners()){
			start.removeActionListener(al);
		}
		
		// assign module methods to buttons
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				module.startWorker();
			}
		});
		
		// remove previous acction listeners
		for(ActionListener al : cancel.getActionListeners()){
			cancel.removeActionListener(al);
		}
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				module.Cancel();
			}
		});
		
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