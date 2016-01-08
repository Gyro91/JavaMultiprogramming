import SynchPort.*;
import FairSem.FairSem;
import MailboxB.MailboxB;
import Message.*;
import MessageQueue.*;

import java.util.ArrayList;
import java.util.List;
import MailboxB.*;

class ConsumerB extends Thread {
    public static SynchPort<Integer> listenConsumer = new SynchPort<Integer>(1);
    String newLine = System.getProperty("line.separator");
    public void run() {
    	Message <Character> service_request = new Message<Character>();
        Message <Integer> m = new Message<Integer>();
    		
		service_request.data = 'r';
		service_request.tid = Thread.currentThread().getId();	
		
		for(int i=0; i<50; i++) {
			
			// Request of Remove
			MailboxB.ready.send(service_request);
			// Taking Data
			m = listenConsumer.receive();
			
			System.out.println("#Consumer received " + m.data + " from " +
					"Thread[" + m.tid + "]" + newLine);
		}
	}
}

class ProducerB extends Thread {
	private int priority;
	
	public ProducerB(int i) {
		priority = i;
	}
	
	public void run() {
		Message <Character> service_request = new Message<Character>();
		Message <Integer> m = new Message<Integer>();
		SynchPort<Character> goProd = new SynchPort<Character>(1);
		
		// Elaborating request
		service_request.data = 'i';
		service_request.tid = Thread.currentThread().getId();
		service_request.priority = priority;
		service_request.response = goProd;
					
		
		for (int i=0; i<5; i++) {
			
			// Elaborating Data
			m.data = i;
			m.tid = Thread.currentThread().getId();
			 
			// Request of Inserting
			MailboxB.ready.send(service_request);

			// Wait an ACK
			goProd.receive();

			// Sending Data
			MailboxB.listenProducers.send(m);			

		}
	}
}


public class TestPortWithMailboxB {
	static ConsumerB consumer = new ConsumerB();
	static ProducerB producers [] = new ProducerB[10];
	static MailboxB server = new MailboxB(11, 50, ConsumerB.listenConsumer);

	public static void main(String[] args) {
		server.start();
		consumer.start();
		for(int i=0; i<10; i++) {
			producers[i] = new ProducerB(i+1);
			producers[i].start();
		}

	}

}
