import GivenTools.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;



public class RUBTClient {
    	
	
	public static HashMap<ByteBuffer, Object> response;
	public static ServerSocket serverSocket = null;
	public static int port;
	
	

	public static void main(String[] args) throws  IOException {
		
		if(args.length != 2)
		{
			
			System.err.println("Invalid number of command line inputs: some_file.torrent new_file_name.mov ");
			return;
		}
		
		/*stores command line input*/
		File resultFile = new File(args[1]);
		File torrentFile = new File(args[0]);
		
		
		/*check if the torrent file exists*/
		if(!torrentFile.exists())
		{
			System.err.print(args[0]+" does not exist");
			return;
		}
	    /*create TorrentInfo object */
		TorrentInfo torrentInfo = Utils.parseTorrent(torrentFile);
		if(torrentInfo == null)
		{
			System.err.println("Unable to parse torrentFile");
			return;
		}
        /*if true then serverSocket was opened successfully*/
		if(findAPort(port))
		{
			Tracker tracker = new Tracker(torrentInfo, serverSocket, port);
			/*Possibly where to call Manager*/
			Manager manager = new Manager(tracker,resultFile);
			Thread t = new Thread(manager);
			t.start();
			if(true){
				  return;
			   }
			
	    }
	}				
	/*returns true if serverSocket was opened successfully*/
	public static boolean findAPort(int port)
	{
		
		
	    port = 6882;
		while(port<= 6889)
		{
			try
			{
				serverSocket = new ServerSocket(port);
				return true;
			} catch(IOException e){
				System.err.println("Unable to obtain port: " + port);
			}
		}
		
		return false;
	}
	
}
	
	
