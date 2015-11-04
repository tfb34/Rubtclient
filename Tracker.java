import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;







import GivenTools.*;

public class Tracker {
	int uploaded = 0;
	int downloaded = 0;
	int port;
	int left;
	byte[] info_hash;
	byte[] ourPeerId;
	URL newURL;
	private byte[] responseArray;
	int interval;
	
	/*response will obtain the dictionary obtained from get request*/
	private HashMap<ByteBuffer, Object> response;
	ServerSocket serverSocket;
	TorrentInfo torrentInfo;
	
	public Tracker(TorrentInfo torrentInfo, ServerSocket serverSocket, int port)
	{
		this.port = port;
		this.left = torrentInfo.file_length;
		this.info_hash = torrentInfo.info_hash.array();
		this.serverSocket = serverSocket;
		this.torrentInfo = torrentInfo;
	}
	
	/*SENDS GET REQUEST for dictionary of peers*/
	@SuppressWarnings("unchecked")
	public HashMap<ByteBuffer, Object> getResponse()
	{
		ourPeerId = Utils.getRandomPeerId();
		newURL  = Utils.createURL(torrentInfo, port, ourPeerId, info_hash, uploaded, downloaded, left);
		//newURL  = Utils.createURL(torrentInfo, port, ourPeerId, info_hash, uploaded, downloaded, left);
		try {
 			HttpURLConnection httpConnection = (HttpURLConnection)newURL.openConnection();
 			
 			int responseCode = httpConnection.getResponseCode();
 			System.out.println("Response code:"+ responseCode);
 			DataInputStream dataInputStream = new DataInputStream(httpConnection.getInputStream());
 			
 			int dataSize = httpConnection.getContentLength();
 			
 			responseArray = new byte[dataSize];
 			
 			dataInputStream.readFully(responseArray);
 			dataInputStream.close();
 			
 			/*if response was not a failure then we should obtain the peer dictionary, response*/
 			if(responseArray==null)
 			{
 				System.err.println("Unable to communicate with tracker");
 
 			}
 			
	 			try {
	 				/*DICTIONARY*/
					response = (HashMap<ByteBuffer, Object>) Bencoder2.decode(responseArray);
					
					if(response.containsKey(Utils.KEY_FAILURE))
					{
						System.out.println("failure from the tracker");
						
					}
					else
					{
						/*INTERVAL*/
						interval = (int)response.get(Utils.KEY_INTERVAL);
						System.out.println("Interval: "+ interval); //FOR TESTING
					}
					
					
				} catch (BencodingException e) {
					// TODO Auto-generated catch block
					System.err.println("Failed to create dictionary");
				}
 			
 		 serverSocket.close();
 			
	      }catch (IOException e) {
	 			System.err.println("Unable to communicate with tracker");
	 			
	 		}
		
	    return response;
	
  }
	
	
   public void printTrackerResponse(byte[] responseArray)
   {
	   /*PRINTS OUTS TRACKER's RESPONSE */

		System.out.println("responseArray message: ");
		InputStream is = null;
		BufferedReader bf = null;
		try{
			is = new ByteArrayInputStream(responseArray);
			bf = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while((temp = bf.readLine()) != null)
			{
				System.out.println(temp);
			}
			System.out.println();
		}catch(IOException e){
			
		}finally{
		   
			try{
				if(is != null) is.close();
			}catch(Exception ex)
		{
			
		}
       }
		
   }
   
}
