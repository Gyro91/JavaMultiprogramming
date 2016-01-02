import SynchPort.*;
import Message.*;
import FairSem.*;


class Sender extends Thread {	
	public SynchPort <Integer> response;
	
	public Sender() {
		response = new SynchPort<Integer>(1);
	}
	
	public void run() {
		Message <Integer> m = new Message<Integer>();	
		int x = 2;
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Sender: Asking for the "
				+ "operation of increment of " + x  );
		
		TestSynchPort.console_mux.V();
		
		// Setting information
		m.data = x;
		m.response = response;
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Sender: Sending Message.. ");
		
		TestSynchPort.console_mux.V();
		
		// Sending data
		TestSynchPort.consumer.sp.send(m);
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Sender: Sending operation completed! ");
		System.out.println("Sender: Waiting for the respons ");
	
		TestSynchPort.console_mux.V();
		
		// Receiving the response 
		m = response.receive();
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Sender: Response value " + m.data);
		
		TestSynchPort.console_mux.V();
	}
}

class Receiver extends Thread {
	public SynchPort <Integer> sp;
	public Receiver() {
		sp = new SynchPort<Integer>(1);
	}
	public void run() {
		int answer;
		Message <Integer> m = new Message<Integer>();
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Receiver: Sleep for a while to test if the send is blocking... ");
		
		TestSynchPort.console_mux.V();
		
		try {
			sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Receiver: Receiving the data ");
		
		TestSynchPort.console_mux.V();
		
		// Receiving the data
		m = TestSynchPort.consumer.sp.receive();
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Receiver: Received data..value = " + m.data);
		
		TestSynchPort.console_mux.V();
		// Elaborating data && sending the answer
		
		answer = m.data++;
		m.response.send(m);
		
		TestSynchPort.console_mux.P();
		
		System.out.println("Receiver: Response Sended! ");
		
		TestSynchPort.console_mux.V();
		
	}
}
public class TestSynchPort {
	static Sender producer = new Sender();
    static Receiver consumer = new Receiver();
    static FairSem console_mux = new FairSem(1, 2, false); // mutex to protect console video
	public static void main(String[] args) {
		producer.start();
		consumer.start();

	}

}
