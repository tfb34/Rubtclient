import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import GivenTools.TorrentInfo;


public class Utils {


	/* KEY_FAILURE and KEY_INTERVAL are two possible responses from the tracker*/
	/** The constant KEY_FAILURE. */
	public static final ByteBuffer KEY_FAILURE = ByteBuffer.wrap(new byte[] {
			'f', 'a', 'i', 'l', 'u', 'r', 'e', ' ', 'r', 'e', 'a', 's', 'o',
			'n' });
	
	
	/** The constant KEY_INTERVAL. */
	public static final ByteBuffer KEY_INTERVAL = ByteBuffer.wrap(new byte[] {
			'i', 'n', 't', 'e', 'r', 'v', 'a', 'l' });
	
	
	
	public static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] {
			'p', 'e', 'e', 'r', 's'});

	public static final ByteBuffer KEY_PEERID = ByteBuffer.wrap(new byte[] {
			'p', 'e', 'e', 'r', ' ','i','d'});
	public static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] {
			'i','p'});
	public static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] {
			'p','o','r','t'});


	@SuppressWarnings("finally")
	public static TorrentInfo parseTorrent(File torrentFile)
	{
		TorrentInfo torrentInfo = null;
		
		/*convert torrentFile into byteArray*/
	    FileInputStream fis;
		try {
			fis = new FileInputStream(torrentFile);
			 // creates a new byte array output stream
		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					 
			byte[] buf = new byte[1024];
					
            for(int readNum; (readNum = fis.read(buf))!= -1; )
				{
					byteArrayOutputStream.write(buf, 0, readNum);
					
				}
					
			byte[] torrentBytes = byteArrayOutputStream.toByteArray(); 
					 
			/*create torrentInfo object using the newly created byte array */
					
			 torrentInfo = new TorrentInfo(torrentBytes);
			//left = torrentInfo.file_length;
		    fis.close();
						
		    }		
			catch (FileNotFoundException e1) {
			System.err.println("torrent file not found");
		    }finally{
		    	return torrentInfo;
		    }
	    
				
	      
		}
	/*create a properly formatted URL by adding certain parameters*/
	public static URL createURL(TorrentInfo torrentInfo, int ourPort, byte[] ourPeerId, byte[] info_hash, int uploaded, int downloaded, int left)
	{
		URL announce_URL = torrentInfo.announce_url;
		
		//ourPeerId = getRandomPeerId();// must be properly escaped also
		
		String newURL = announce_URL.toString();
		info_hash = torrentInfo.info_hash.array();
		newURL += "?info_hash="+properlyEscape(torrentInfo.info_hash.array())+"&peer_id="+properlyEscape(ourPeerId)+"&port="+ourPort+"&uploaded="+uploaded+"&downloaded="+downloaded+"&left="+left+ "&numwant=50" ;
		try {
			return new URL(newURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
		    System.err.println("Unable to create new URL for GET request");
		}
		
		return null;
	}
	

	
	/* converts each byte in the byteArray into its hexadecimal value. e.g.byte[]{0xAF,0xFE} transforms to "%AF%FE  */
	public static String properlyEscape(byte[] byteArray)
	{
	    char[] hexChars = { '0', '1', '2', '3', '4', '5', '6','7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		if (byteArray == null) {
			return null;
		}
		if (byteArray.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(byteArray.length * 2);
		for (byte b : byteArray) {
			byte hi = (byte) ((b >> 4)&0x0f);
			byte lo = (byte) (b & 0x0f);
			sb.append('%').append(hexChars[hi]).append(hexChars[lo]);
		}
		//System.out.println("return info_hash: "+ sb.toString());
		return sb.toString();
		
	}
	
	/*generates a random peer ID*/
	public static byte[] getRandomPeerId() {
		
		Random r = new Random(System.currentTimeMillis());
		
		byte[] peerId = new byte[20];//must be a 20-byte string
        // I'm using the Azureus style convention e.g. '-AZ2060-....'
		// '-', two characters for client id, 4 ascii digits for version number, '-', followed by random numbers 
		peerId[0] = '-';
		peerId[1] = 'R';
		peerId[2] = 'C';
		peerId[3] = '1';
		peerId[4] = '7';
		peerId[5] = '9';
		peerId[6] = '3';
		peerId[7] = '-';

		for (int i = 5; i < 20; i++) {
			peerId[i] = (byte) ('A' + r.nextInt(26));
		}
		
		return peerId;
	}
	
	/*converting (int) bytebuffer to byte array*/
	byte[] unpack(int bytes) {
		  return new byte[] {
		    (byte)((bytes >>> 24) & 0xff),
		    (byte)((bytes >>> 16) & 0xff),
		    (byte)((bytes >>>  8) & 0xff),
		    (byte)((bytes       ) & 0xff)
		  };
	}
	
	//interpeting bitfield[] T_T how?
	
	/*convert bitfield into a boolean array
	 * @param: total number of pieces(integer) and the byte[] which represents the bitfield
	 * @return: boolean[]
	 * the boolean[] is useful b/c e.g. if(bitfieldBoolean[0]== 1) that means the peer has piece 0
	 */
	public static boolean[] ConvertBitfieldToBooleanArray(byte[] bitfield, int numPieces){
		 if(bitfield == null){
			 System.out.println("unable to convert null bitfield to boolean array");
			 return null;
		 }
		 
		 boolean[] arr = new boolean[numPieces];
		 /*indirectly traversing the bits in each byte in the given byte[] bitfield*/
		 for(int i = 0; i<numPieces; i++){
			 int byteIndex = i/8;// 8 b/c there are 8 bits in a byte. ranges from 0 to numPieces-1
			 int bitIndex = i%8; // position within the byte . ranges from 0 to 7
			 
			 if(((bitfield[byteIndex] << bitIndex) & 0x80) == 0x80){
				 arr[i]= true;//peer has piece i
			 }
			 else{
				 arr[i] = false; //peer does not have piece i so don't request it
			 }
			 
		 }
		 return arr;
	}
	
	public static void printBooleanArray(boolean[] arr){
		System.out.println("converted bitfield:" +Arrays.toString(arr));
		
	}
	
}
