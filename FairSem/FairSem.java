package FairSem;
import Fifo_Queue.*;

public class FairSem {
	
	private int semvalue; 				// Value of the Semaphore
	
	private Fifo_Queue blocked;   		// Queue of blocked threads
	private Fifo_Queue awakened;		// Queue of awakened threads
	
	private boolean test_mode;		// If equal 1, FairSem is in test mode
	
	public Fifo_Queue entry_P;		// Queue for testing
	public Fifo_Queue exit_P;		// Queue for testing
	
	public FairSem(int value, int Nthreads, boolean val) {
		semvalue = value;
		blocked = new Fifo_Queue(Nthreads);
		awakened = new Fifo_Queue(Nthreads - value);
		test_mode = val;
		
		if(test_mode) {
			entry_P = new Fifo_Queue(Nthreads);
			exit_P = new Fifo_Queue(Nthreads);
		}
	}
	
	public synchronized void P() {
		long my_tid = Thread.currentThread().getId();
		
		if (test_mode)
			entry_P.insert(my_tid);
	
		if (semvalue == 0) {
			
			// If sem is red, blocking
			blocked.insert(my_tid);

			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Only the selected thread must go forward 
			// in the critical section
			while (awakened.first() != my_tid)
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
			}
			awakened.remove();
			/* If the semaphore is initialized with a value = n (>1)
			 * and in the situation where there are n threads in the critical section
			 * and other threads are blocked. If there are some V (greater than 1) 
			 * of the the n threads, if the threads that  
			 * */
			if(!awakened.empty())
				notifyAll();
		}
		else
			semvalue--;
		
		if (test_mode)
			exit_P.insert(my_tid);
	}
	
	public synchronized void V() {		
				
		if(!blocked.empty())	{
			awakened.insert(blocked.remove());
			notifyAll();
		}
		else
			semvalue++;
	}
}
