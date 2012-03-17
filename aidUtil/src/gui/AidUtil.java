package gui;

import java.awt.BorderLayout;
import java.awt.Menu;
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

public class AidUtil extends JFrame {
	private static final long serialVersionUID = -3377472963066131706L;

	Core core;
	
	JTextField targetPath;
	JTextArea logArea;
	JPanel optionPanel, controlPanel;
	JButton start, cancel;
	List<MaintenanceModule> modules;
	JMenuBar mBar;
	JMenu moduleMenu;
	
	public AidUtil(Core core, List<MaintenanceModule> modules){
		this.core = core;
		this.modules = modules;
		init();
	}
	
	private void init(){
		setSize(700, 500);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar mBar = new JMenuBar();
		setJMenuBar(mBar);
		mBar.add(moduleMenu = new JMenu("Module"));
		
		
		for(MaintenanceModule mm : modules){
			moduleMenu.add(new JMenuItem(mm.getClass().getSimpleName()));
		}
		
		controlPanel = new JPanel();
		controlPanel.add(targetPath = new JTextField(30));
		controlPanel.add(start = new JButton("Start"));
		controlPanel.add(cancel = new JButton("Cancel"));
		
		add(controlPanel, BorderLayout.NORTH);
		add(optionPanel = new JPanel(), BorderLayout.CENTER);
		add(new JScrollPane( logArea = new JTextArea(10,30) ),BorderLayout.SOUTH);
	}
	
}
