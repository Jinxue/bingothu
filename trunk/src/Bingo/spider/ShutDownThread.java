package Bingo.spider;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

import Bingo.index.IndexManager;

// The ShutdownThread is the thread we pass to the
// addShutdownHook method
public class ShutDownThread extends Thread {
	
	private IndexManager indexManager = null;
	
	public ShutDownThread(IndexManager indexManager) {    
		super();    
		this.indexManager = indexManager;
		}    
	public void run() {   
	   	try {
			this.indexManager.closeIndex();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		System.out.println("[Shutdown thread] Shutting down");    
	}
}
