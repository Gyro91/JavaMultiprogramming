package MailboxA;

import Message.Message;
import MessageQueue.MessageQueue;
import SynchPort.SynchPort;

public class MailboxA extends Thread {
	private int countExtractions, pendingProducers;
	private MessageQueue mq;
	public static SynchPort <Integer> listenProducers;	// For the communication with Producers
	public static SynchPort <Character> ready;	// Port for waiting a service request
	private static SynchPort <Integer> listenConsumer;
	
	public MailboxA(int Nproducers, int extractions, SynchPort <Integer> cons) {
		countExtractions = extractions; // number of extractions that must handle the Mailbox
		pendingProducers = 0;
		listenConsumer = cons;
		listenProducers = new SynchPort<Integer>(Nproducers);	
		ready = new SynchPort<Character>(Nproducers + 1);
		mq = new MessageQueue();
	}
	
	public void run() {
		Message <Character> service = new Message<Character>();
		Message <Integer> m = new Message<Integer>();
		
		while (countExtractions > 0) {

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

				if (service.data == 'r') {
					// Remove Operation
					switch(mq.how_many()) {
						case 0:	
								/* If the buffer is empty, 
								 * waiting for an insert of a Producer */
								
								service = ready.receive();	
								m = listenProducers.receive();
	
								mq.insert(m);
								/* After inserting we can serve the Consumer */
								
								listenConsumer.send(mq.extract());
								countExtractions--;
								break;
						default:
								listenConsumer.send(mq.extract());
								countExtractions--;
								break;
					}
				}

				else if (service.data == 'i') {
					// Insert Operation
					switch(mq.how_many()) {
						case 4:
							/* If the buffer is full, we must wait for a request
							 * of the Consumer. Requests of Producers are saved to
							 * be served later!
							 */
							
							while (service.data == 'i') {
								pendingProducers++;
								service = ready.receive();
							}
							
							if (service.data == 'r') {							
								listenConsumer.send(mq.extract());
								countExtractions--;
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