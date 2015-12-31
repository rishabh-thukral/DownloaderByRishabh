package RDownload;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

public class DownloadsTableModel extends AbstractTableModel implements Observer{
	//column names
	private static final String[] columnNames = {"URL","Size","Progress","Status"};
	//classes for each column
	private static final Class[] columnClasses = {String.class,String.class,JProgressBar.class,String.class};
	// download list
	private ArrayList<Download> downloadList = new ArrayList<Download>();
	// add new download
	public void addDownload(Download download){
		//register for notifications of download changes
		download.addObserver(this);
		downloadList.add(download);
		fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	}
	//download from specified row
	public Download getDownload(int row){
		return downloadList.get(row);
	}
	//remove from list
	public void clearDownload(int row){
		downloadList.remove(row);
		fireTableRowsDeleted(row, row);
	}
	//column count
	public int getColumnCount(){
		return columnNames.length;
	}
	//column name
	public String getColumnName(int col){
		return columnNames[col];
	}
	//get column class
	public Class<?> getColumnClass(int col){
		return columnClasses[col];
	}
	//get tables row count
	public int getRowCount(){
		return downloadList.size();
	}
	public Object getValueAt(int row,int col){
		Download download = downloadList.get(row);
		switch(col){
		case 0://URL
			return download.getURL();
		case 1://size
			return download.getSize();
		case 2://progress
			return download.getProgress();
		case 3://Status
			return Download.STATUSES[download.getStatus()];
		}
		return "";
	}
	public void update(Observable o,Object arg){
		int index = downloadList.indexOf(o);
		fireTableRowsUpdated(index, index);
	}
}
