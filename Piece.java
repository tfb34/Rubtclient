/*A block NOT a piece */
public class Piece extends Message {
	 /*Payload*/
	  int index;
	  int begin;
      byte[] block;
	/*constructor*/
	public Piece(int index, int begin, byte[] block){
		super(9+block.length, PIECE_ID ); //length prefix should always be 9+16384? right
		this.index = index;
		this.begin = begin;
		this.block = block;
	}
	
	public void PrintMessage(){
		System.out.print("Length Prefix: "+length+"  ID: "+ID+ "  Payload: <index = "+index+"><begin = "+begin+"><block = "+block.length+">");
		System.out.println();
		return;
	}
}
