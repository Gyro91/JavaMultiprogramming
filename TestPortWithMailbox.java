import SynchPort.*;
import FairSem.FairSem;
import Message.*;
import MessageQueue.*;
 
class Mailbox extends Thread {
	private int countExtractions, pendingProducers;
	private MessageQueue mq;
	public static SynchPort <Integer> listenProducers;	// For the communication with Producers
	public static SynchPort <String> ready;	// Port for waiting a service request

	public Mailbox() {
		countExtractions = 0;
		pendingProducers = 0;
		listenProducers = new SynchPort<Integer>(10);	
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
	
								mq.insert(m);
								/* After inserting we can serve the Consumer */
								
								Consumer.listenConsumer.send(mq.extract());
								countExtractions++;
								break;
						default:
								Consumer.listenConsumer.send(mq.extract());
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
								Consumer.listenConsumer.send(mq.extract());
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
    public static SynchPort<Integer> listenConsumer = new SynchPort<Integer>(1);
	public void run() {
		Message <String> service_request = new Message<String>();
		service_request.data = new String("Remove");
		service_request.tid = Thread.currentThread().getId();
		Message <Integer> m = new Message<Integer>();
		
		for(int i=0; i<50; i++) {
			
			// Request of Remove
			Mailbox.ready.send(service_request);
			// Taking Data
			m = listenConsumer.receive();
				
			System.out.println("Consumer received " + m.data + " from " +
					"Thread[" + m.tid + "]");

		}
	}
}

class Producer extends Thread {
	public void run() {
		Message <String> service_request = new Message<String>();
		service_request.data = new String("Insert");
		service_request.tid = Thread.currentThread().getId();
		Message <Integer> m = new Message<Integer>();
		
		for(int i=0; i<5; i++) {
			
			// Elaborating Data
			m.data = i;
			m.tid = Thread.currentThread().getId();
			
			// Request of Inserting
			Mailbox.ready.send(service_request);
		
			// Sending Data
			Mailbox.listenProducers.send(m);			


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

