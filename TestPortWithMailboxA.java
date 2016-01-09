import SynchPort.*;
import Message.*;
import MailboxA.*;

class ConsumerA extends Thread {
    public static SynchPort<Integer> listenConsumer = new SynchPort<Integer>(1);
    
	public void run() {
		Message <Character> service_request = new Message<Character>();
		service_request.data = 'r';
		service_request.tid = Thread.currentThread().getId();
		Message <Integer> m = new Message<Integer>();
		
		for(int i=0; i<50; i++) {
			
			// Request of Remove
			MailboxA.ready.send(service_request);
			// Taking Data
			m = listenConsumer.receive();
				
			System.out.println("Consumer received " + m.data + " from " +
					"Thread[" + m.tid + "]");

		}
	}
}

class ProducerA extends Thread {
	public void run() {
		Message <Character> service_request = new Message<Character>();
		service_request.data = 'i';
		service_request.tid = Thread.currentThread().getId();
		Message <Integer> m = new Message<Integer>();
		
		for(int i=0; i<5; i++) {
			
			// Elaborating Data
			m.data = i;
			m.tid = Thread.currentThread().getId();
			
			// Request of Inserting
			MailboxA.ready.send(service_request);
		
			// Sending Data
			MailboxA.dataProducers.send(m);			


		}
	}
}


public class TestPortWithMailboxA {
	static ConsumerA consumer = new ConsumerA();
	static ProducerA producers [] = new ProducerA[10];
	static MailboxA server = new MailboxA(11, 50, ConsumerA.listenConsumer);

	public static void main(String[] args) {
		server.start();
		consumer.start();
		for(int i=0; i<10; i++) {
			producers[i] = new ProducerA();
			producers[i].start();
		}

	}

}
