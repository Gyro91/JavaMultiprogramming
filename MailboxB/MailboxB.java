package MailboxB;

import java.util.ArrayList;
import java.util.List;

import Message.Message;
import MessageQueue.MessageQueue;
import SynchPort.SynchPort;

class Blocked {
	public long tid;	// Thread-id of the blocked Thread
	public int priority; // Priority of the blocked thread
	public SynchPort <Character> goProd; // Port where the blocked thread is waiting an ACK
}

public class MailboxB extends Thread {
	private int countExtractions; // Number of extraction on the buffer
	private MessageQueue mq;  // Buffer of 4 integer
	private List <Blocked> blockedProducers; // List of Blocked producers
	
	public static SynchPort <Integer> listenProducers;	// For the communication with Producers
	public static SynchPort <Character> ready;	// Port for waiting a service request
	private static SynchPort <Integer> listenConsumer;
	
	static Message <Character> service = new Message<Character>();
	static Message <Character> ack = new Message<Character>();
	static Message <Integer> m = new Message<Integer>();
	
	String newLine = System.getProperty("line.separator");
	
	public MailboxB(int Nproducers, int extractions, SynchPort <Integer> cons) {
		countExtractions = extractions; // number of extractions that must handle the Mailbox
		listenConsumer = cons;
		blockedProducers = new ArrayList<Blocked>(Nproducers);
		listenProducers = new SynchPort<Integer>(Nproducers);	
		ready = new SynchPort<Character>(Nproducers + 1);
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

		while (countExtractions > 0) {
			
			if (blockedProducers.size() > 0 && mq.how_many() < 4) {
				// Serving pending requests is more important!
				while (mq.how_many() < 4 && blockedProducers.size() > 0) {
					/* Pending requests are served 
					 * with the respect of the buffer constraint
					 */	
					Blocked thread = blockedProducers.remove(maxPriority());
					
					System.out.println("#Awakening Thread[" + thread.tid + "]"
							+ " with priority " + thread.priority + newLine);
					
					// Awakening
					thread.goProd.send(ack);

					m = listenProducers.receive();
					mq.insert(m);
				}
			}
			else {
				/* Waiting for a request of a service */
				service = ready.receive();		
				
				if (service.data == 'r') {
					switch(mq.how_many()) {
						case 0:	
								/* If the buffer is empty, 
								 * waiting for an insert of a Producer */
								service = ready.receive();
								service.response.send(ack);
								
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
					switch(mq.how_many()) {
						case 4:
							/* If the buffer is full, we must wait for a request
							 * of the Consumer. Requests of Producers are saved to
							 * be served later!
							 */

							System.out.println("#Mailbox is full" + newLine);
							
							while (service.data == 'i') {
								Blocked thread = new Blocked();
								
								thread.tid = service.tid;
								thread.goProd = service.response;
								thread.priority = service.priority;
					
								System.out.println("#Blocking Thread[" + thread.tid
										+ "] with priority " + thread.priority + newLine);

								
								blockedProducers.add(0, thread);

								service = ready.receive();
							}
							
							if (service.data == 'r') {							
								listenConsumer.send(mq.extract());
								countExtractions--;
								
								System.out.println("#Mailbox is not full" + newLine);
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


