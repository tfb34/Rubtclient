import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.security.*;

import GivenTools.TorrentInfo;
import GivenTools.ToolKit;

public class Peer extends Thread{
	
	public byte[] peerid;
	public String ip;
	public int port;//given to peer by main() 
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private ByteArrayOutputStream outputStream;
	private Socket socket;
	private Tracker tracker;
	LockedVariables lockedVariables;
	private TorrentInfo torrentInfo;
	private int numPieces;
	private boolean[] peerBitfield;
	Thread t;
   /*newly added*/
	
	public static byte[] identifierProtocol= new byte[] {'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
   /*1) peer should be able to send handshake*/


	public Peer( byte[] peerid, String ip, int port, Tracker tracker, LockedVariables lockedVariables){
		this.peerid=peerid;
		this.ip=ip;
		this.port=port;
		this.tracker = tracker;
		this.lockedVariables =  lockedVariables;
		this.torrentInfo = tracker.torrentInfo;
		this.numPieces = torrentInfo.file_length/torrentInfo.piece_length;// does not include the last piece
	}
	
	public void run(){
		 /*open socket, open streams to write/read to and fro*/
		try {
			socket = new Socket(ip,port);
			dataOutputStream= new DataOutputStream(socket.getOutputStream());
     	    dataInputStream = new DataInputStream(socket.getInputStream());
     	    outputStream = new ByteArrayOutputStream();
     	    if(SendHandShake()){
     	    	System.out.println("handshake complete");
     	    	/*PEER SENDS YOU A BITFIELD MSG*/
          	  Message m = Message.GetAndDecodeMessage(dataInputStream,socket, -1);
          	  m.PrintMessage();
          	  Bitfield m2 =(Bitfield)m;
          	  m2.PrintMessage();
          	  //obtain the peer Bitfield for later use
          	  int numPieces = (int)Math.ceil((double) tracker.torrentInfo.file_length/(double)tracker.torrentInfo.piece_length);
          	  peerBitfield = Utils.ConvertBitfieldToBooleanArray(m2.bitfield, numPieces);
  
          	  
  
          	  
          	  /*SEND INTERESTED MSG*/
	          	if(Message.SendMessage(Message.INTERESTED_MSG, dataOutputStream)){
	     		   System.out.println("sent Interested msg");
	     	   }
	          	/*PEER SENDS YOU  AN UNCHOKE MSG*/
          	    Message k = Message.GetAndDecodeMessage(dataInputStream,socket, -1);
          	     k.PrintMessage();
          	   /*send first request*/
	           	  System.out.println("piece length: "+torrentInfo.piece_length);
	           	  System.out.println("GETTING PIECES");
	           	  //start downloading time start
	           	  Download();
	           	  //end downloading time
	           	  System.out.println("Download is done");
	           	  
     	    }else{//if handshake did not work then close connection with peer
     	    	try{
         	    	socket.close();
         	    	}catch(SocketException e){
         	    		e.printStackTrace();
         	    	}
     	    	
     	    	return;
     	    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("unable to open peer socket");
			e.printStackTrace();
		}
	}
	
	/*SENDS HANDSHAKE*/
	public boolean SendHandShake(){
		if(outputStream == null){
			System.out.println("ByteArrayOutputStream is null. cannot send handshake.");
			return false;
		}else{
			 byte pstr = (byte) 19;//1 byte
     	     try {
     	    	outputStream.write(pstr);
				outputStream.write(identifierProtocol);
				byte[] reservedBytes = new byte[8];
        	    outputStream.write(reservedBytes);
        	    byte[] infoHash = tracker.info_hash;
        	    outputStream.write(infoHash);
        	    byte[] peerID = tracker.ourPeerId;//length is 20 bytes
        	    outputStream.write(peerID);
        	    /*handshakeMessage*/
        	    
       	        byte[] handshakeMessage = outputStream.toByteArray();
       	        System.out.println("length of handshake  "+handshakeMessage.length);//68 bytes
       	        System.out.println(new String(handshakeMessage,"ASCII"));
       	        
       	        /*send handshake*/
       	        dataOutputStream.write(handshakeMessage);
       	       /*obtaining message*/
        	    byte[] messageReturned = new byte[68];
        	    socket.setSoTimeout(150000);
        	   
        	    dataInputStream.readFully(messageReturned);
        	    /*make sure tracker info hash and this info hash returned in messageReturned are the same */
         	     byte[] hinfo = Arrays.copyOfRange(messageReturned, 28, 48);//last index is excluded
        	    if(Arrays.equals(hinfo, infoHash)){
         		   System.out.println("handshake complete");
         		   return true;
         	    } else{
         		   System.err.println("info_hash do not match");
         		   //socket.close();
         		   return false;
         	   }
         	   /*HANDSHAKE WORKS*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
    public boolean PeerConnect(){
    	try {
			socket = new Socket(ip,port);
			dataOutputStream= new DataOutputStream(socket.getOutputStream());
     	    dataInputStream = new DataInputStream(socket.getInputStream());
     	    outputStream = new ByteArrayOutputStream();
     	    if(SendHandShake()){
     	    	System.out.println("handshake complete");
     	    	/*PEER SENDS YOU A BITFIELD MSG*/
          	  Message m = Message.GetAndDecodeMessage(dataInputStream,socket,-1);
          	  m.PrintMessage();
          	  Bitfield m2 =(Bitfield)m;
          	  System.out.println("bitfield length m2: "+ m2.length);
          	  
              /*SEND INTERESTED MSG*/
	          	if(Message.SendMessage(Message.INTERESTED_MSG, dataOutputStream)){
	     		   System.out.println("sent Interested msg");
	     	   }
	          	/*PEER SENDS YOU  AN UNCHOKE MSG*/
          	    Message k = Message.GetAndDecodeMessage(dataInputStream,socket, -1);
          	     k.PrintMessage();
          	   return true;// unchoke
     	    }else{//if handshake did not work then close connection with peer
     	    	try{
         	    	socket.close();
         	    	}catch(SocketException e){
         	    		e.printStackTrace();
         	    	}
     	    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("unable to open peer socket");
			e.printStackTrace();
		}
    	return false;
    }

		
	

	
	

	

	public static boolean verifySHA1(TorrentInfo info,byte[] piece, int index){
    	
    	byte[] sha1Piece = null;
    	try{
    		MessageDigest md = MessageDigest.getInstance("SHA-1");
    		sha1Piece = md.digest(piece); 
    	} catch (NoSuchAlgorithmException e){
    		System.err.println(e);
    	}
    	
    	byte[] hashFromTor = info.piece_hashes[index].array();
    	
    	if(Arrays.equals(sha1Piece, hashFromTor)){
    		return true;
    	} else{
    		return false;
    	}
    	
    }
	
	
	public void Download(){//instead of true , add a value that can be changed from manager. so if the user cancels then its possible to stop downloading
		while(true){
		
		    int pieceIndex = lockedVariables.GetPieceIndexToDownload();//synchronized
			    if(pieceIndex<numPieces){// check that the peer has that piece
			    	if(peerBitfield[pieceIndex]){//check if the peer has the piece
				    	byte[] piece = GetThatPiece(pieceIndex, torrentInfo.piece_length);
				    	if(piece!= null){
				    		while(!verifySHA1(this.torrentInfo, piece, pieceIndex)){//verify the piece
				    			System.out.println("Resent request because something was wrong with the piece");
				    			piece = GetThatPiece(pieceIndex, torrentInfo.piece_length);
				    		}
				    	  lockedVariables.piecesDownloaded[pieceIndex] = piece;//put piece in the 2d array
				    	  lockedVariables.updateBitfield(pieceIndex); // update bitfield 
				    	}
			    	}else{//peer does not have piece
			    		lockedVariables.PutPieceIndexBack(pieceIndex);//return pieceIndex so another Peer object  can download it 
			    	}
			    }else{//pieceIndex is that of the last piece
			    	if(peerBitfield[pieceIndex]){
				    	System.out.println("there is a piece that is smaller than the specified piece_length");
				    	int remainderBlocks = torrentInfo.file_length%torrentInfo.piece_length;
				    	byte[] piece = GetThatPiece(pieceIndex, remainderBlocks);
					    	if(piece!= null){
					    		lockedVariables.piecesDownloaded[pieceIndex] = piece;//put piece in the 2d array
					    		lockedVariables.updateBitfield(pieceIndex);//update bitfield
					    		return;//temporary solution 
					    	}
			    	}else{
			    		lockedVariables.PutPieceIndexBack(pieceIndex);
			    	}
			    }
			
			
		}
	}//End of Download()
	//void
	//while(true), get element from queue%lock,  use get that piece function, obtain the piece/ byte[], verify if not last piece, put piece in 2-d array%lock, update bitfield%lock
	
	
	public byte[] GetThatPiece(int index, int pieceLength){
		byte[] piece = new byte[pieceLength];
		ByteBuffer target = ByteBuffer.wrap(piece);
		int numblocks = pieceLength/16384;
		int remainderBytes = pieceLength%16384;
		int begin = 0; // position of where to put the block in the piece byte[]
		System.out.println("piecelength("+pieceLength+")"+"/"+"16384"+"="+numblocks);
		
		
			for(int i = 0; i<numblocks; i++){
				
				/*create the request*/
				Request requestMsg = new Request(index, begin, 16384);//<index><begin><length>
				/*send the request*/
				Message.SendMessage(requestMsg, dataOutputStream);
		
					/*Retrieve the peer message*/
					Message msgReturned = Message.GetAndDecodeMessage(dataInputStream,socket, 16384);
					if(msgReturned.ID == Message.PIECE_ID){
						/*get the block byte[] that the piece msg has*/
						byte[] theBlock = ((Piece)msgReturned).block;
						/*add 'theBlock' byte[] into the piece byte[]*/
						target.put(theBlock);
			            begin += 16384;
					}else{
						System.out.println("Message returned in GetThatBlock() was not a piece message");
						return null;
					}
				} 
		 
		/*takes care of those blocks that are less than 16384*/
		
		if(remainderBytes !=0){
			System.out.println("piece length requested: "+ remainderBytes);
			Request requestMsg = new Request(index, begin, remainderBytes);
			requestMsg.PrintMessage();
			Message.SendMessage(requestMsg, dataOutputStream);
			Message msgReturned = Message.GetAndDecodeMessage(dataInputStream,socket,remainderBytes);
				if(msgReturned == null){
					System.out.println("message is null");
				}else if(msgReturned.ID == Message.PIECE_ID){
						byte[] theBlock = ((Piece)msgReturned).block;
						target.put(theBlock);
				}
				else{
					System.out.println("could not obtain the remainder bytes");
					return null;
				}
				
			
		}//end of remainder Bytes if statement
		return piece;
	}
	
	
}

	