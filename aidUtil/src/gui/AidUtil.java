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
		setSize(700, 500);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar mBar = new JMenuBar();
		setJMenuBar(mBar);
		mBar.add(moduleMenu = new JMenu("Module"));
		
		
		for(MaintenanceModule mm : modules){
			JMenuItem jmi = new JMenuItem(mm.getClass().getSimpleName());
			jmi.addActionListener(this);
			moduleMenu.add(jmi);
			guiModelMap.put(jmi, mm);
		}
		
		controlPanel = new JPanel();
		controlPanel.add(targetPath = new JTextField(30));
		
		add(controlPanel, BorderLayout.NORTH);
		add(optionPanel = new JPanel(), BorderLayout.CENTER);
		add(new JScrollPane( logArea = new JTextArea(10,30) ),BorderLayout.SOUTH);
	}
	
	private void setActiveModule(final MaintenanceModule module){
		optionPanel.removeAll();
		module.optionPanel(optionPanel);
		module.setLog(logArea);
		
		JButton start = new JButton("Start");
		JButton cancel = new JButton("Cancel");
		
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
		if(e.getSource() instanceof JMenuItem){
			setActiveModule(guiModelMap.get(e.getSource()));
		}
	}
	
}
