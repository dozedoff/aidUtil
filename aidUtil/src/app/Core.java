package app;

import gui.AidUtil;

public class Core {
	AidUtil aidUtil;
	
	public static void main(String args[]){
		new Core().startCore();
	}
	
	public void startCore(){
		//TODO create and start connection pool
		
		aidUtil = new AidUtil(this);
		aidUtil.setVisible(true);
	}
}
