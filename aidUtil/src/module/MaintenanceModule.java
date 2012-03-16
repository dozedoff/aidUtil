package module;

import io.ConnectionPool;

import java.awt.Container;

import javax.swing.JTextArea;

public interface MaintenanceModule {
	public void optionPanel(Container container);
	public void start(ConnectionPool connPool);
	public void Cancel();
	public void setLog(JTextArea logArea);
}
