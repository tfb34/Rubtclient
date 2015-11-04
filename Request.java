import java.io.DataOutputStream;
import java.io.IOException;



public class Request extends Message {
/*<len=0013><id=6><index><begin><length>*/
	
/*
 index: integer specifying the zero-based piece index
begin: integer specifying the zero-based byte offset within the piece
length: integer specifying the requested length.  */
	
	int index;
	int begin;
	int requestedLength;
	public Request(int index, int begin, int length) {
		super(13, REQUEST_ID);
		// TODO Auto-generated constructor stub
		this.index = index;
		this.begin  = begin;
		this.requestedLength = length;
		
	}

	//the sendMessage() in the Message class already sent the length prefix and messageID to the peer.
	//this function sends the payload
	public void SendPayLoad(DataOutputStream dataOutputStream){
		System.out.println("Request sendpayload was called");
		try {
			dataOutputStream.writeInt(index);
			dataOutputStream.writeInt(begin);
			dataOutputStream.writeInt(requestedLength);
			System.out.println("Request payload has been sent");
		} catch (IOException e) {
			System.err.println("Unable to send Request payload");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    
	}
	
	public void PrintMessage(){
		System.out.println("PrintMessage:  Length Prefix: "+length+"  ID: "+ID+"  Request Payload");
		System.out.print("index: "+index+"  begin: "+begin+"  length: "+length);
		return;
	}

}
