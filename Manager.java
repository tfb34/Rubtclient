import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;



public class Manager extends Thread {
 
	LockedVariables lockedVariables;
    Tracker tracker;
 
   HashMap<ByteBuffer, Object> response;
   /*list of Peers with ID 'RU11"*/
   ArrayList<Peer> listOfValidPeers= new ArrayList<Peer>();
   ArrayList<Thread> threadList = new ArrayList<Thread>();
    byte[] theFile ;
	int numPieces;
	int numPiecesDownloaded=0;
	
	ServerSocket serverSocket;
	int listenPort = -1;
	File output = null;
	  public Manager(Tracker tracker, File output){
		  this.tracker = tracker;
		  this.theFile = new byte[tracker.torrentInfo.file_length];
		  this.numPieces= (int)Math.ceil((double) tracker.torrentInfo.file_length/(double)tracker.torrentInfo.piece_length);
		  this.lockedVariables= new LockedVariables(numPieces);
		  this.output = output;
	  }
	  
	  public synchronized void updatePiecesDownloaded(){
		  this.numPiecesDownloaded += 1;
		  
	  }
	  
	  
	  public void run(){
		  response = tracker.getResponse();
		  if(response == null){
			  System.out.println("Unable to retrieve dictionary from tracker");
			  return;
		  }
		  // I want to create a thread that continuously prints out the progress until 100%
		  if(response.containsKey(Utils.KEY_INTERVAL)){
			    Thread t1 = new Thread(new Runnable(){
			    	public void run(){
			    		try {
							PrintProgress();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    	}
			    });
			    t1.start();
	            ConnectToPeers();
		  }
		  writeToFile();
		
		  
	  }
	  
	  public void PrintProgress() throws InterruptedException{
		  while(this.numPiecesDownloaded<this.numPieces){// ex . 2 pieces out of 434 are downloaded
			  Thread.sleep(1000);
			  double x = (double)numPiecesDownloaded/ (double)this.numPieces;
			  x = x*100;
			  System.out.print("\r[");
			  System.out.print("Downloading... "+(int)x+"%");
		  }
	  }
	  public void writeToFile(){
		  FileOutputStream out;
			try {
				out = new FileOutputStream(output);
				  System.out.println("writing to file");
				  for(int i = 0; i<numPieces; i++){
					  if(lockedVariables.piecesDownloaded[i]!=null){
						  try {
							out.write(lockedVariables.piecesDownloaded[i]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					  }
				  }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			  
	  }
	 
	  
	  public void ConnectToPeers(){
		  @SuppressWarnings("unchecked")
			ArrayList<Object> peersList =(ArrayList<Object>) response.get(Utils.KEY_PEERS);
			  //traverse the peersList and obtain only the ones with RU11
			  for(int i=0;i<peersList.size();i++){
					try{
						@SuppressWarnings("unchecked")
						HashMap<ByteBuffer,Object> peerDictionary=(HashMap<ByteBuffer,Object>)peersList.get(i);
						ByteBuffer id=(ByteBuffer)peerDictionary.get(Utils.KEY_PEERID);
						byte[] peerId=id.array();//peer ID
				       String tempid=new String(peerId,"ASCII");
				       
							if(tempid.contains("RU11")){
								/*add 'peerDictionary' to the listOfValidPeers, obtain the id , ip, port*/
								 ByteBuffer IP  = (ByteBuffer)peerDictionary.get(Utils.KEY_IP);
							     String IPAddress = new String(IP.array(), "ASCII");//Peer IP
							    
							     if(IPAddress.equals("128.6.171.131")){// only creates peers/downloaders with this IP address
									     int peerPort = (int)peerDictionary.get(Utils.KEY_PORT);//peer Port
										 Peer peer = new Peer(peerId, IPAddress, peerPort,tracker, lockedVariables, this);//if im correct all the peers have access to the same lockedVariables
										 listOfValidPeers.add(peer);
							     }
							}
					
					}catch(UnsupportedEncodingException e){
						System.out.println("Unable to create a peer list");
						System.out.println(e);
					}
			  }
			  /*create,launch, and add the threads to list*/
			  for(int x = 0; x<listOfValidPeers.size(); x++){
				  System.out.println("IP of peer"+x+":"+listOfValidPeers.get(x).ip);
				  Thread t= new Thread(listOfValidPeers.get(x));
				  t.start();
				  threadList.add(t);
			  }   
			  // if IP is "123.0.0.0" then start add as Peer and start otherwise create a different type of peer that listens?
			  long start = System.currentTimeMillis();
			  for(int x=0; x<threadList.size(); x++){
				  try {
					threadList.get(x).join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("unable to call join()");
					e.printStackTrace();
				}
			  }
			  long end = System.currentTimeMillis();
			  System.out.println("\nTotal Downloading time: "+ (end-start));
			
			  
			  
	  }
	  
	  
}
