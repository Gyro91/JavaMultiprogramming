import SynchPort.*;
import FairSem.FairSem;
import Message.*;
import MessageQueue.*;

import java.util.ArrayList;
import java.util.List;
 

class Blocked {
	public long tid;	// Thread-id of the blocked Thread
	public int priority; // Priority of the blocked thread
	public SynchPort <String> goProd; // Port where the blocked thread is waiting an ACK
}

class MailboxB1 extends Thread {
	private int countExtractions; // Number of extraction on the buffer
	private MessageQueue mq;  // Buffer of 4 integer
	private List <Blocked> blockedProducers; // List of Blocked producers
	
	public static SynchPort <Integer> listenProducers;	// For the communication with Producers
	public static SynchPort <String> ready;	// Port for waiting a service request
	static Message <String> service = new Message<String>();
	static Message <String> ack = new Message<String>();
	static Message <Integer> m = new Message<Integer>();
	String newLine = System.getProperty("line.separator");
	
	public MailboxB1(int Nproducers) {
		countExtractions = 0;
		blockedProducers = new ArrayList<Blocked>(Nproducers);
		listenProducers = new SynchPort<Integer>(Nproducers);	
		ready = new SynchPort<String>(Nproducers + 1);
		mq = new MessageQueue();
	}
	
	// Return the index of the thread with max Priority
	public int maxPriority() {
		int max = 0, index = 0;
		for (int i=0; i<blockedProducers.size(); i++) {
			if (blockedProducers.get(i).priority > max) {
				max = blockedProducers.get(i).priority;
				index = i;
			}
		}
		return index;
	}
	
	public void run() {

		while (countExtractions != 50) {
			
			if (blockedProducers.size() > 0 && mq.how_many() < 4) {
				// Serving pending requests is more important!
				while (mq.how_many() < 4 && blockedProducers.size() > 0) {
					/* Pending requests are served 
					 * with the respect of the buffer constraint
					 */	
					Blocked thread = blockedProducers.remove(maxPriority());
					
					TestPortWithMailboxB.console_mux.P();
					System.out.println("#Awakening Thread[" + thread.tid + "]"
							+ " with priority " + thread.priority + newLine);
					TestPortWithMailboxB.console_mux.V();
					
					// Awakening
					thread.goProd.send(ack);

					m = listenProducers.receive();
					mq.insert(m);
				}
			}
			else {
				/* Waiting for a request of a service */
				service = ready.receive();		
				
				if (service.data.equals("Remove")) {
					switch(mq.how_many()) {
						case 0:	
								/* If the buffer is empty, 
								 * waiting for an insert of a Producer */
								service = ready.receive();
								service.response.send(ack);
								
								m = listenProducers.receive();
	
								mq.insert(m);
								/* After inserting we can serve the Consumer */
								
								ConsumerB1.listenConsumer.send(mq.extract());
								countExtractions++;
								break;
						default:
								ConsumerB1.listenConsumer.send(mq.extract());
								countExtractions++;
								break;
					}
				}

				else if (service.data.equals("Insert")) {
					switch(mq.how_many()) {
						case 4:
							/* If the buffer is full, we must wait for a request
							 * of the Consumer. Requests of Producers are saved to
							 * be served later!
							 */
							
							TestPortWithMailboxB.console_mux.P();
							System.out.println("#Mailbox is full" + newLine);
							TestPortWithMailboxB.console_mux.V();
							
							while (service.data.equals("Insert")) {
								Blocked thread = new Blocked();
								
								thread.tid = service.tid;
								thread.goProd = service.response;
								thread.priority = service.priority;
								
								TestPortWithMailboxB.console_mux.P();

								System.out.println("#Blocking Thread[" + thread.tid
										+ "] with priority " + thread.priority + newLine);
								
								TestPortWithMailboxB.console_mux.V();
								
								blockedProducers.add(0, thread);

								service = ready.receive();
							}
							
							if (service.data.equals("Remove")) {							
								ConsumerB1.listenConsumer.send(mq.extract());
								countExtractions++;
								
								TestPortWithMailboxB.console_mux.P();
								System.out.println("#Mailbox is not full" + newLine);
								TestPortWithMailboxB.console_mux.V();
							}
							break;
						
						default:
							service.response.send(ack);
							m = listenProducers.receive();
						    mq.insert(m);
						    break;
					}
				}
			}
		}
	}
}

class ConsumerB1 extends Thread {
    public static SynchPort<Integer> listenConsumer = new SynchPort<Integer>(1);
    String newLine = System.getProperty("line.separator");
    public void run() {
    	Message <String> service_request = new Message<String>();
        Message <Integer> m = new Message<Integer>();
    		
		service_request.data = new String("Remove");
		service_request.tid = Thread.currentThread().getId();	
		
		for(int i=0; i<50; i++) {
			
			// Request of Remove
			MailboxB1.ready.send(service_request);
			// Taking Data
			m = listenConsumer.receive();
			
			TestPortWithMailboxB.console_mux.P();
			System.out.println("#Consumer received " + m.data + " from " +
					"Thread[" + m.tid + "]" + newLine);
			TestPortWithMailboxB.console_mux.V();
		}
	}
}

class ProducerB1 extends Thread {
	
	private int priority;
	
	public ProducerB1(int i) {
		priority = i;
	}
	
	public void run() {
		Message <String> service_request = new Message<String>();
		Message <Integer> m = new Message<Integer>();
		SynchPort<String> goProd = new SynchPort<String>(1);
		String newLine = System.getProperty("line.separator");
		
		// Elaborating request
		service_request.data = new String("Insert");
		service_request.tid = Thread.currentThread().getId();
		service_request.priority = priority;
		service_request.response = goProd;
					
		
		for (int i=0; i<5; i++) {
			
			// Elaborating Data
			m.data = i;
			m.tid = Thread.currentThread().getId();
			 
			// Request of Inserting
			MailboxB1.ready.send(service_request);

			// Wait an ACK
			goProd.receive();

			// Sending Data
			MailboxB1.listenProducers.send(m);			

		}
	}
}


public class TestPortWithMailboxB {
	static ConsumerB1 consumer = new ConsumerB1();
	static ProducerB1 producers [] = new ProducerB1[10];
	static MailboxB1 server = new MailboxB1(10);
	static FairSem console_mux = new FairSem(1, 11, false); // mutex to protect console video

	public static void main(String[] args) {
		server.start();
		consumer.start();
		for(int i=0; i<10; i++) {
			producers[i] = new ProducerB1(i+1);
			producers[i].start();
		}

	}

}
