import SynchPort.*;
import Message.*;
import MessageQueue.*;

class Mailbox extends Thread {
	private int countExtractions, pendingProducers;
	private MessageQueue mq;
	public SynchPort <Integer> listenProducers;	// For the communication with Producers
	public SynchPort <Integer> listenConsumer;	// For the communication with the Consumer
	public SynchPort <String> ready;	// To select the first ready

	public Mailbox() {
		countExtractions = 0;
		pendingProducers = 0;
		listenProducers = new SynchPort<Integer>(10);
		listenConsumer = new SynchPort<Integer>(1);
		ready = new SynchPort<String>(11);
		mq = new MessageQueue();
	}
	public void run() {
		Message <String> who = new Message<String>();
		Message <Integer> m = new Message<Integer>();
		while(countExtractions != 50) {

			if (pendingProducers > 0 && mq.how_many() < 4) {
				while(mq.how_many() < 4 && pendingProducers > 0) {
					m = listenProducers.receive();
					mq.insert(m);
					pendingProducers--;
				}
			}
			else {

				who = ready.receive();		
				System.out.println("Server: who's next " + who.data + " how-many " + mq.how_many());

				if (who.data.equals("Consumer")) {
					switch(mq.how_many()) {
						case 0:	who = ready.receive();	
								m = listenProducers.receive();
								listenConsumer.send(m);
								countExtractions++;
								break;
						default:
								listenConsumer.send(mq.extract());
								countExtractions++;
								break;
					}
				}

				else if (who.data.equals("Producer")) {
					switch(mq.how_many()) {
					case 4:	
							while (who.data.equals("Producer")) {
							pendingProducers++;
							who = ready.receive();
							System.out.println("pending: "+ pendingProducers + " " + who.data);
							}
							
							if (who.data.equals("Consumer")) {							
								listenConsumer.send(mq.extract());
								countExtractions++;}
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
		who.data = new String("Consumer");
		Message <Integer> m = new Message<Integer>();
		for(int i=0; i<50; i++) {
			System.out.println("Consumer sends ready signal");
			TestPortWithMailbox.server.ready.send(who);
			System.out.println("Consumer starts receiving");
			m = TestPortWithMailbox.server.listenConsumer.receive();
			System.out.println("Consumer received " + m.data + " from" +
					"Thread[" + m.tid + "]");
		}
	}
}

class Producer extends Thread {
	public void run() {
		Message <String> who = new Message<String>();
		who.data = new String("Producer");
		Message <Integer> m = new Message<Integer>();
		for(int i=0; i<5; i++) {
			TestPortWithMailbox.server.ready.send(who);
			m.data = i;
			m.tid = Thread.currentThread().getId();
			TestPortWithMailbox.server.listenProducers.send(m);
			System.out.println("Thread[" + m.tid + "]" +
					"sended " + m.data);
		}
	}
}


public class TestPortWithMailbox {
	static Consumer consumer = new Consumer();
	static Producer producers [] = new Producer[10];
	static Mailbox server = new Mailbox();
	public static void main(String[] args) {
		server.start();
		consumer.start();
		for(int i=0; i<10; i++) {
			producers[i] = new Producer();
			producers[i].start();
		}

	}

}

