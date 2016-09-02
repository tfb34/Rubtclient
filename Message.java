import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;



public class Message {
/*Types of messages and their Ids*/
    //keepAlive has no ID 
	public static final byte KEEPALIVE_ID = -1; // meaning keepalive msgs don't have IDs
	public static final byte CHOKE_ID = 0;
	public static final byte UNCHOKE_ID = 1;
	public static final byte INTERESTED_ID = 2;
	public static final byte UNINTERESTED_ID = 3;
	public static final byte HAVE_ID = 4;
	public static final byte BITFIELD_ID = 5;
	public static final byte REQUEST_ID = 6;
	public static final byte PIECE_ID = 7;
	public static final byte CANCEL_ID = 8;//optional
	public static final byte PORT_ID = 9;//optional
	
/*Constant Messages*/
	public static final Message CHOKE_MSG = new Message(1, CHOKE_ID);
	public static final Message UNCHOKE_MSG = new Message(1, UNCHOKE_ID);
	public static final Message INTERESTED_MSG = new Message(1, INTERESTED_ID);
	public static final Message UNINTERESTED_MSG = new Message(1, UNINTERESTED_ID);
	
	
	/*Every message has to have a length(integer) and ID(byte)*/
	int length;
	byte ID;

	/*constructor*/
	// not all msgs have payload so we'll let encoding() take care of that
	public Message(int length, byte ID){
		this.length = length;
		this.ID = ID;
	}
	
	/*if message is a Message then there is no payload. if message is a Request then the other SendPayload
	 * function is called*/
	public void SendPayLoad(DataOutputStream dataOutputStream){
		return;
	}
	
	public void PrintMessage(){
		System.out.println("Length Prefix: "+length+"  ID: "+ID);
		return;
	}
	
	//Message functions should be creating msgs, encoding(basically prep
	// msg before writing it to the Peer), decoding.. thats all i got so far
	
	/*Encoding message in order to send it to Peer*/
	//what are we encoding? a message 
	//@Param: Message and OutputStream
	// so far only sends length prefix and message ID to peer
	public static boolean SendMessage(Message message, DataOutputStream dataOutputStream){
		if(message == null){
			System.err.println("message null. unable to send");
			return false;
		}
		else{//we have a message. write to the peer: length, id, and mayb payload
			try {
				dataOutputStream.writeInt(message.length);
				if(message.length>0){// then send the ID 
			       dataOutputStream.writeByte(message.ID);
					//check which Ids have payload and call addPayload()
			        
					message.SendPayLoad(dataOutputStream);//if message is a Request then the sendpayload() in the Request class is called otherwise no payload is sent
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("unable to sendMessage");
				e.printStackTrace();
			}
		
		}return true;
	}
	
	/*@Param: inputstream ,
	 * @return: a Message object
	 reads from inputstream and creates a message 
	 don't forget to check if there is a payload, if there is you have to add it
	 need to two contructors for Message. one that contains the payload*/
   public static Message GetAndDecodeMessage(DataInputStream dataInputStream, Socket socket, int blockLength){
	    try {
	    	//DataInputStream dataInputStream = new DataInputStream(inputStream);
	    	//not sure if we are to expect keep alive msgs
	    	socket.setSoTimeout(150000);
			int lengthPrefix = dataInputStream.readInt();
			if(lengthPrefix == 0){//if true then it's a keep alive Message
				Message message = new Message(lengthPrefix, (byte)-1);
				return message;
			}else{
				// the message consists of an ID
				byte messageID = dataInputStream.readByte();
				//use switch message to determine what message to return 
				switch(messageID){// check if it falls from 0 to 11
				case(CHOKE_ID)://no payload
					return CHOKE_MSG;
				case(UNCHOKE_ID)://no payload
					return UNCHOKE_MSG;
				case(INTERESTED_ID)://no payload
					return INTERESTED_MSG;
				case(UNINTERESTED_ID)://no payload
					return UNINTERESTED_MSG;
				case(HAVE_ID):// use extension class 1. create have msg 2. return that msg
					int peerIndex = dataInputStream.readInt();
					Message haveMsg = new Have(peerIndex);
					return haveMsg;
				case(BITFIELD_ID):
					byte [] bitfield = new byte[lengthPrefix-1]; //X bytes in the array
				    dataInputStream.readFully(bitfield);
				    Message bitfieldMsg = new Bitfield(bitfield);
					return bitfieldMsg;
				case(REQUEST_ID):
					System.out.println("You got a request msg");
					break;
				case(PIECE_ID):
					int index = dataInputStream.readInt();
				    int begin = dataInputStream.readInt();
				    byte[] block = new byte[blockLength];
				    dataInputStream.readFully(block);
				    
				    Message pieceMsg = new Piece(index,begin, block);
					return pieceMsg;
				case(CANCEL_ID):
					System.out.println("you got a cancel msg");
					break;
				case(PORT_ID):
					System.out.println("you got a port_id msg");
					break;
				}
			}
			
			//now check if 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("error with reading from inputStream");
			e.printStackTrace();
		}
	    
	  return null;
   }
   /*Have messages. the only difference between this and a Message is that this has the peerIndex value*/
   //<len=0005><id=4><piece index>
   public static class Have extends Message {//Have inherits abstract methods and static values in the Message class
	    int peerIndex;
		public Have( int peerIndex) {
			super(5, HAVE_ID);//constructs a Message object. 5 is the constant length prefix of a Have msg
			this.peerIndex = peerIndex;
			// TODO Auto-generated constructor stub
		}

	}
   //END of HAVE message class
   
   /**/
}











