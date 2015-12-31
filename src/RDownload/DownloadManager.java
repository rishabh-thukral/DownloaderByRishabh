package RDownload;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DownloadManager extends JFrame implements Observer{
	private JTextField addTextField;
	private DownloadsTableModel tableModel;
	private JTable table;
	private JButton pauseButton, resumeButton;
	private JButton cancelButton , clearButton;
	private Download selectedDownload;
	private boolean clearing;
	public DownloadManager(){
		setTitle("Downloader By Rishabh");
		setSize(640,480);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				actionExit();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionExit();
			}
		});
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		JPanel addPanel = new JPanel();
		addTextField = new JTextField(30);
		addPanel.add(addTextField);
		JButton addButton = new JButton("Add Download");
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionAdd();
			}
		});
		addPanel.add(addButton);
		tableModel = new DownloadsTableModel();
		table = new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				tableSelectionChanged();
			}
		});
		//only one row should be selected at a time
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//setup progress bar
		ProgressRenderer renderer = new ProgressRenderer(0,100);
		renderer.setStringPainted(true);//show progress text
		table.setDefaultRenderer(JProgressBar.class, renderer);
		//set row height to fit progressbar
		table.setRowHeight((int)renderer.getPreferredSize().getHeight());
		//SETUP DOWNLOAD PANEL
		JPanel downloadsPanel = new JPanel();
		downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
		downloadsPanel.setLayout(new BorderLayout());
		downloadsPanel.add(new JScrollPane(table),BorderLayout.CENTER);
		//setup buttons panel
		JPanel buttonsPanel = new JPanel();
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionPause();
				
			}
		});
		pauseButton.setEnabled(false);
		buttonsPanel.add(pauseButton);
		resumeButton = new JButton("Resume");
		resumeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionResume();
				
			}
		});
		resumeButton.setEnabled(false);
		buttonsPanel.add(resumeButton);
		cancelButton  = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionCancel();
				
			}
		});
		cancelButton.setEnabled(false);
		buttonsPanel.add(cancelButton);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionClear();
				
			}
		});
		clearButton.setEnabled(false);
		buttonsPanel.add(clearButton);
		// add panels to display
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(addPanel,BorderLayout.NORTH);
		getContentPane().add(downloadsPanel,BorderLayout.CENTER);
		getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
	}
	//exit this program
	private void actionExit(){
		System.exit(0);
	}
	//Add new download
	private void actionAdd(){
		URL verifiedUrl = verifyUrl(addTextField.getText());
		if(verifiedUrl!=null){
			tableModel.addDownload(new Download(verifiedUrl));
			addTextField.setText("");
		}else{
			JOptionPane.showMessageDialog(this, "Invalid Download URL","Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	//verify url
	private URL verifyUrl(String url){
		//only http urls 
		//https wont work i think
		if(!url.toLowerCase().startsWith("http://")){
			return null;
		}
		URL verifiedUrl = null;
		//verify format
		try{
			verifiedUrl = new URL(url);
		}catch(Exception e){
			return null;
		}
		//make sure url specifies a file
		if(verifiedUrl.getFile().length()<2){
			return null;
		}
		return verifiedUrl;
	}
	//called when table row selection changes
	private void tableSelectionChanged()
	{
		/*unregister from recving notifications frm last selected download*/
		if(selectedDownload != null){
			selectedDownload.deleteObserver(DownloadManager.this);
		}
		/*if not in the middle of clearing a download,
		 * set the selected download and register to recive notifications from it.*/
		if(!clearing && table.getSelectedRow()>-1){
			selectedDownload = tableModel.getDownload(table.getSelectedRow());
			selectedDownload.addObserver(DownloadManager.this);
			updateButtons();
		}
	}
	private void actionPause(){
		selectedDownload.pause();
		updateButtons();
	}
	private void actionResume(){
		selectedDownload.resume();
		updateButtons();
	}
	private void actionCancel(){
		selectedDownload.cancel();
		updateButtons();
	}
	private void actionClear(){
		clearing = true;
		tableModel.clearDownload(table.getSelectedRow());
		clearing  = false;
		selectedDownload = null;
		updateButtons();
	}
	private void updateButtons(){
		if(selectedDownload != null){
			int status = selectedDownload.getStatus();
			switch(status){
				case Download.DOWNLOADING:
					pauseButton.setEnabled(true);
					resumeButton.setEnabled(false);
					cancelButton.setEnabled(true);
					clearButton.setEnabled(false);
					break;
				case Download.PAUSED:
					pauseButton.setEnabled(false);
					resumeButton.setEnabled(true);
					cancelButton.setEnabled(true);
					clearButton.setEnabled(false);
					break;
				case Download.ERROR:
					pauseButton.setEnabled(true);
					resumeButton.setEnabled(false);
					cancelButton.setEnabled(false);
					clearButton.setEnabled(true);
					break;
				default://complete or canceled
					pauseButton.setEnabled(false);
					resumeButton.setEnabled(false);
					cancelButton.setEnabled(false);
					clearButton.setEnabled(true);
			}
		}
		else{
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}
	@Override
	public void update(Observable o, Object arg) {
		if(selectedDownload!=null && selectedDownload.equals(o)){
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					updateButtons();
					
				}
			});
		}
		
	}
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				DownloadManager manager = new DownloadManager();
				manager.setVisible(true);
			}
		});
	}
}
