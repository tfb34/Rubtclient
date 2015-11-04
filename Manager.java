import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
//wait for 132, filter out 130
//ram used to store what was downloaded even if incomplete
/*this class manages a peer*/
/*1) should */
// create a class that contains all the variables that will be accessed by different threads
public class Manager extends Thread {
  /*the pieces that have not been downloaded. only their piece numbers are in the list*/
  // Queue<Integer> queue = new LinkedList<Integer>();
  /*Pieces downloaded*/
  // byte[][] piecesDownloaded;
  /*Tracker object, contains the our generated peerID sent to the tracker*/
	LockedVariables lockedVariables;
    Tracker tracker;
  /*obtains information on */
   HashMap<ByteBuffer, Object> response;
   /*list of Peers with ID 'RU11"*/
   ArrayList<Peer> listOfValidPeers= new ArrayList<Peer>();
   ArrayList<Thread> threadList = new ArrayList<Thread>();
    byte[] theFile ;
	//ByteBuffer target = ByteBuffer.wrap(theFile); use this when all the threads finish so after the join() 
	int numPieces;
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
	  public void run(){
		  response = tracker.getResponse();
		  if(response == null){
			  System.out.println("Unable to retrieve dictionary from tracker");
			  return;
		  }
		  
		  if(response.containsKey(Utils.KEY_INTERVAL)){
	            ConnectToPeers();
		  }
		  writeToFile();
		
		  
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
										 Peer peer = new Peer(peerId, IPAddress, peerPort,tracker, lockedVariables);//if im correct all the peers have access to the same lockedVariables
										 listOfValidPeers.add(peer);
							     }//else create a different type of peer that wants to download from you
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
			  System.out.println("Total Downloading time: "+ (end-start));
			
			  
			  
	  }
}
