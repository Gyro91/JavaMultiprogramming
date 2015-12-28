import FairSem.*;

class Worker extends Thread {
	public static FairSem fs = new FairSem(3, 10, true);
	public void run() {
		fs.P();
		System.out.println("Thread[" + 
					Thread.currentThread().getId()
					+ "] in the critical section");
	    fs.V();
	}
}

public class TestFairSem {
	static long tid1;
	static long tid2;
	static boolean error = false;
	public static Worker[] workers = new Worker[10];
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(int i=0; i<10; i++){
			workers[i]=new Worker();
			workers[i].start();
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		System.out.println("Start Test!");
		/* The test checks if the sequence of threads that enters in P()
		 * is the same that exits from P() */
		for(int i=0; i<10; i++) {
			tid1=Worker.fs.entry_P.remove();
			tid2=Worker.fs.exit_P.remove();					
			if ( tid1 != tid2)
				error = true;
		}
		
		if (error)
			System.out.println("Test Failed!");
		else
			System.out.println("Test Successed!");
	}

}