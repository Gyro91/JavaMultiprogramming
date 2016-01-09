package MailboxA;

import Message.Message;
import MessageQueue.MessageQueue;
import SynchPort.SynchPort;

public class MailboxA extends Thread {
	private int countExtractions, pendingProducers;
	private MessageQueue mq; // Buffer
	public static SynchPort <Integer> dataProducers;	// For the communication with Producers
	public static SynchPort <Character> ready;	// Port for waiting a service request
	private static SynchPort <Integer> dataConsumer; // Port of the Consumer. It's private because this port is declared
	// public in the receiving process. It's here in order to separate the code of Mailbox from the test application
	
	public MailboxA(int Nproducers, int extractions, SynchPort <Integer> cons) {
		countExtractions = extractions; // number of extractions that must handle the Mailbox
		pendingProducers = 0;
		dataConsumer = cons;
		dataProducers = new SynchPort<Integer>(Nproducers);	
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
					m = dataProducers.receive();
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
								m = dataProducers.receive();
	
								mq.insert(m);
								/* After inserting we can serve the Consumer */
								
								dataConsumer.send(mq.extract());
								countExtractions--;
								break;
						default:
								dataConsumer.send(mq.extract());
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
								dataConsumer.send(mq.extract());
								countExtractions--;
							}
							break;
						
						default:
							m = dataProducers.receive();
						    mq.insert(m);
						    break;
					}
				}
			}
		}
	}
}