import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;


public class LockedVariables {
	/*the pieces that have not been downloaded*/
    public volatile Queue<Integer> piecesToDownload = new LinkedList<Integer>();// when queue is empty thread should stop or exit
    /*the pieces*/
	public volatile byte[][] piecesDownloaded;
	/*pieces that we have*/
	public volatile boolean[] ourBitfield;
	public final ReentrantLock lock= new ReentrantLock();
	
	public LockedVariables(int numPieces){
		//essentially contains the piece IDs which we must send requests for.
		for(int i = 0; i<numPieces; i++){
			this.piecesToDownload.add(i);
		}
		this.piecesDownloaded = new byte[numPieces][];
		this.ourBitfield = new boolean[numPieces]; //initialize to all 0's 
		Arrays.fill(this.ourBitfield, false);
	}
	
	public void enter(){
		lock.lock();//may be the opposite
	}
	
	public void leave(){
		lock.unlock();
		
	}
	public synchronized boolean[] GetOurBitfield(){
		return ourBitfield;
	}
	
	public synchronized void updateBitfield(int pieceIndex){
		this.ourBitfield[pieceIndex] = true;
	}
	
	
	public synchronized int GetPieceIndexToDownload(){
		int pieceIndex;
		if(piecesToDownload.size()!=0){
		   pieceIndex = piecesToDownload.remove();
		}else{
			pieceIndex =-1;
		}
		return pieceIndex;
	}
	public synchronized void PutPieceIndexBack(int pieceIndex){
		piecesToDownload.add(pieceIndex);
	}
	
	public void printLockedInfo(){
		int havePieces = 0;
		for(int i = 0; i<piecesDownloaded.length; i++){
			if(piecesDownloaded[i] !=null){
				havePieces+=1;
			}
		}
		System.out.println("Pieces Downloaded: "+ havePieces);
	}
	
}