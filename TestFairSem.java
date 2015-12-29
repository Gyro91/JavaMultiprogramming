import FairSem.*;
import java.util.Scanner;

class Worker extends Thread {
	public static FairSem fs;
	public static int SharedResource = 0;
	public void run() {
		
		fs.P();
		
		// Doing some stuff
		
		for (int i=0; i<100; i++)
			SharedResource++;
		
		fs.V();
	}
}

public class TestFairSem {
	public static Worker[] workers;
	
	public static void main(String[] args) {
		long tid1, tid2;
		boolean error = false;
		Scanner scan= new Scanner(System.in);
		int ntasks, semvalue;
		
		// Take input number of Tasks for the Test
		
		System.out.println("Insert number of tasks that access"
				+ " to the Resource");
		ntasks = scan.nextInt();
		workers = new Worker[ntasks];
		
		// Take value of the semaphore for the Test

		System.out.println("Insert value of the Semaphore");
		semvalue = scan.nextInt();
		if ( semvalue <= 0) {
			System.out.println("Value of the Semaphore not consistent");
			System.exit(1);
		}
		
		Worker.fs = new FairSem(semvalue, ntasks, true);
		
		// Creating Tasks for the test
		
		for (int i=0; i<ntasks; i++) {
			
			System.out.println("Generating Task " + i );
			workers[i]=new Worker();
			
			System.out.println("Starting Task " + i );
			workers[i].start();
		}
		
		// Joining on Tasks
		
		for (int i=0; i<ntasks; i++)
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (!Worker.fs.entry_P.full() &&
			!Worker.fs.exit_P.full()) {
			System.out.println("Error in the simulation");
			System.exit(1);		
		}
		
		System.out.println("Start Test!");
			
		/* The test checks if the sequence of threads that enters in P()
		 * is the same that exits from P() */
		
		for (int i=0; i<ntasks; i++) {
			tid1=Worker.fs.entry_P.remove();
			tid2=Worker.fs.exit_P.remove();					
			if ( tid1 != tid2)
				error = true;
		}
		
		// Printing the result of the test
		
		if (error)
			System.out.println("Test Failed!");
		else
			System.out.println("Test Successed!");
	}

}