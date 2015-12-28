package FairSem;
import Fifo_Queue.*;

public class FairSem {
	private int semvalue; 				// Value of the Semaphore
	private boolean next = true;	 	// If true thread selected, has woken up
	private long next_thread;  			// Id of the next thread that must go for first 
										// among blocked threads
	private Fifo_Queue fq;   			// Queue of blocked threads
	
	private boolean test_mode;
	public Fifo_Queue entry_P;
	public Fifo_Queue exit_P;
	
	public FairSem(int value, int dim, boolean val) {
		semvalue = value;
		fq = new Fifo_Queue(dim);
		test_mode = val;
		if(test_mode) {
			entry_P = new Fifo_Queue(dim);
			exit_P = new Fifo_Queue(dim);
		}
	}
	
	public synchronized void P() {
		long my_tid = Thread.currentThread().getId();
		
		if (test_mode)
			entry_P.insert(my_tid);
	
		if (semvalue == 0) {
			// If sem is red, blocking
			fq.insert(my_tid);

			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			while (next_thread != my_tid || semvalue == 0)
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			next = true;

			if (semvalue > 1) {
				/* If there are n blocked threads and m threads did V(),
				 * the m of n threads must be awakened here,
				 * otherwise they are blocked even if the semvalue != 0
				 */
				next_thread = fq.remove();
				notifyAll();
			}
				
		}
		// Decreasing semaphore value
		semvalue--;
		
		if (test_mode)
			exit_P.insert(my_tid);
	}
	
	public synchronized void V() {
		long my_tid = Thread.currentThread().getId();
		semvalue++;		
		if(!fq.empty() && next)	{
			next_thread = fq.remove();
			next = false;
			notifyAll();
		}
		
	}
}
