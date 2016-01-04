import SynchPort.*;
import FairSem.FairSem;
import Message.*;
import MessageQueue.*;
 
class Mailbox extends Thread {
	private int countExtractions, pendingProducers;
	private MessageQueue mq;
	public SynchPort <Integer> listenProducers;	// For the communication with Producers
	public SynchPort <Integer> listenConsumer;	// For the communication with the Consumer
	public SynchPort <String> ready;	// Port for waiting a service request

	public Mailbox() {
		countExtractions = 0;
		pendingProducers = 0;
		listenProducers = new SynchPort<Integer>(10);
		listenConsumer = new SynchPort<Integer>(1);
		ready = new SynchPort<String>(11);
		mq = new MessageQueue();
	}
	
	public void run() {
		Message <String> service = new Message<String>();
		Message <Integer> m = new Message<Integer>();
		
		while(countExtractions != 50) {

			if (pendingProducers > 0 && mq.how_many() < 4) {
				// Serving pending requests is more important!
				while (mq.how_many() < 4 && pendingProducers > 0) {
					/* Pending requests are served 
					 * with the respect of the buffer constraint
					 */	
					m = listenProducers.receive();
					mq.insert(m);
					pendingProducers--;
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
								m = listenProducers.receive();
								
								/* After inserting we can serve the Consumer */
								
								listenConsumer.send(m);
								countExtractions++;
								break;
						default:
								listenConsumer.send(mq.extract());
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
							
							while (service.data.equals("Insert")) {
								pendingProducers++;
								service = ready.receive();
							}
							
							if (service.data.equals("Remove")) {							
								listenConsumer.send(mq.extract());
								countExtractions++;
							}
							break;
						
						default:
							m = listenProducers.receive();
						    mq.insert(m);
						    break;
					}
				}
			}
		}
	}
}

class Consumer extends Thread {
	public void run() {
		Message <String> who = new Message<String>();
		who.data = new String("Remove");
		Message <Integer> m = new Message<Integer>();
		
		for(int i=0; i<50; i++) {
			
			// Request of Remove
			TestPortWithMailbox.server.ready.send(who);
			// Taking Data
			m = TestPortWithMailbox.server.listenConsumer.receive();
			
			TestPortWithMailbox.console_mux.P();
			System.out.println("Consumer received " + m.data + " from " +
					"Thread[" + m.tid + "]");
			TestPortWithMailbox.console_mux.V();
		}
	}
}

class Producer extends Thread {
	public void run() {
		Message <String> service_request = new Message<String>();
		service_request.data = new String("Insert");
		Message <Integer> m = new Message<Integer>();
		
		for(int i=1; i<=5; i++) {
			
			// Elaborating Data
			m.data = i;
			m.tid = Thread.currentThread().getId();
			
			// Request of Insert
			TestPortWithMailbox.server.ready.send(service_request);
		
			// Sending Data
			TestPortWithMailbox.server.listenProducers.send(m);
			
			TestPortWithMailbox.console_mux.P();
			System.out.println("Thread[" + m.tid + "] " +
					"sended " + m.data);
			TestPortWithMailbox.console_mux.V();
		}
	}
}


public class TestPortWithMailbox {
	static Consumer consumer = new Consumer();
	static Producer producers [] = new Producer[10];
	static Mailbox server = new Mailbox();
	static FairSem console_mux = new FairSem(1, 11, false); // mutex to protect console video

	public static void main(String[] args) {
		server.start();
		consumer.start();
		for(int i=0; i<10; i++) {
			producers[i] = new Producer();
			producers[i].start();
		}

	}

}

