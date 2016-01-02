package SynchPort;
import Message.*;
import FairSem.*;

public class SynchPort <T> {
	private Message <T> data;
	private FairSem empty, full, waitR;
	
	/* Nsenders are the number of the senders that can use the port */
	
	public SynchPort(int Nsenders) {
		
		empty = new FairSem(1, 1, false);
		waitR = new FairSem(0, 1, false);
		full = new FairSem(0, Nsenders, false);
	}
	
	public void send(Message<T> m) {
		empty.P();
		data = m;
		full.V();
		waitR.P();			
	}
	
	public Message <T> receive() {
		
		Message <T> m = new Message <T>();
		
		full.P();
		m = data;
		empty.V();		
		waitR.V();
		
		return m;
	}
	
}
