package SynchPort;
import Message.*;
import FairSem.*;

public class SynchPort <T> {
	private Message <T> data;
	private FairSem empty, full, waitR;
		
	public SynchPort(int Nsenders) {		
		empty = new FairSem(1, Nsenders, false);
		waitR = new FairSem(0, Nsenders, false);
		full = new FairSem(0, 1, false);
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
