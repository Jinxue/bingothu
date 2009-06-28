package Bingo.spider;

public class SaveVisitedDataThread extends Thread{
	
	Spider spider = null;
	
	public SaveVisitedDataThread (Spider s){
		super();
		spider = s ;
	}
	
	public void run(){
		
		spider.storeVisitedData();
		
		System.out.println("Store!!");
		
	}
	
}
