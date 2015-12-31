package RDownload;

import java.io.*; // for InputStream & RandomAccessFIle 
import java.net.*; // for URL
import java.util.Observable;

public class Download extends Observable implements Runnable {
	private static final int MAX_BUFFER_SIZE = 1024;
	public static final String STATUSES[] = {
		"Downloading" , "Paused" , "Complete" , "Cancelled" , "Error"
	};
	
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;
	private URL url;//link download
	private float size;//size of file download in bytes
	private float downloaded;//size that has been downloaded
	private int status;//current download status
	
	public Download(URL url){
		this.url = url;
		size = -1;
		downloaded = 0;
		status  = DOWNLOADING;
		//to initiate download
		download();
	}
	public String getURL(){
		return url.toString();
	}
	public float getSize(){
		return size;
	}
	public float getProgress(){
		return (size==0)?0:((downloaded/size) * 100);
	}
	public int getStatus(){
		return status;
	}
	public void pause(){
		status = PAUSED;
		stateChanged();
	}
	public void resume(){
		status = DOWNLOADING;
		stateChanged();
	}
	public void cancel(){
		status = CANCELLED;
		stateChanged();
	}
	private void error(){
		status = ERROR;
		stateChanged();
	}
	private void download(){
		Thread th = new Thread(this);
		th.start();
	}
	private String getFileName(URL url){
		String fileName = url.getFile();
		return fileName.substring(1+fileName.lastIndexOf('/'));
	}
	public void run(){
		RandomAccessFile file = null;
		InputStream stream = null;
		try{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("RANGE ", "BYTES = " + downloaded + "-");
			//connect to server
			connection.connect();
			// response code should be in 200 range
			if(connection.getResponseCode() / 100 != 2){
				error();
			}
			//content length should be valid
			int contentLength = connection.getContentLength();
			if(contentLength<1){
				error();
			}
			//modify the size if it hasn't been done
			if(size==-1){
				size = contentLength;
				stateChanged();
			}
			//open file and seek to the end of it
			file = new RandomAccessFile(getFileName(url),"rw");
			file.seek((long)downloaded);
			stream = connection.getInputStream();
			while(status == DOWNLOADING){
				byte buffer[];
				if(size-downloaded > MAX_BUFFER_SIZE){
					buffer = new byte[MAX_BUFFER_SIZE];
				}
				else{
					buffer = new byte[(int)(size-downloaded)];
				}
				///read from server into buffer
				int read  = stream.read(buffer);
				if(read==-1){
					break;
				}
				//write buffer to file
				file.write(buffer, 0, read);
				downloaded+=read;
				stateChanged();
			}
			if(status == DOWNLOADING){
				status  = COMPLETE;
				stateChanged();
			}
		}
		catch(Exception e){
			error();
		}
		finally{
			// close file
			if(file != null){
				try{
					file.close();
				}catch(Exception e){}
			}
			//close server connection
			if(stream!=null){
				try{
					stream.close();
				}catch(Exception e){}
			}
		}
	}
	// notifying observers
	private void stateChanged(){
		setChanged();
		notifyObservers();
	}
}
