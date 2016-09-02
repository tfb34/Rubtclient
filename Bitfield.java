
public class Bitfield extends Message {
//<len=0001+X><id=5><bitfield>
//what is a bitfield? 
	byte[] bitfield;
	
	public Bitfield(byte[] bitfield) {
		//this.bitfield = bitfield;
		super(1+bitfield.length, BITFIELD_ID);//not 1 but 1+length of bitfield
		this.bitfield = bitfield;
		// TODO Auto-generated constructor stub
	}
	
	
	public void PrintMessage(){
		System.out.print("Length Prefix: "+length+"  ID: "+ID+ "  Payload: bitfield");
		return;
	}

}//think of how to send this message, maybe? do we even have to send a bitfield


