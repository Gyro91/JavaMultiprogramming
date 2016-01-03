package MessageQueue;
import Message.*;

class Message_T {
	int data;
	int tid;
}

public class MessageQueue {
	private Message_T buffer [];
	private int rear, front, count;
	public MessageQueue() {
		buffer = new Message_T[4];
		for(int i=0; i<4; i++)
			buffer[i] = new Message_T();
		rear = front = count = 0;
	}
	
	public synchronized void insert(Message<Integer> m) {
		buffer[rear].data = m.data;
		buffer[rear].tid = (int) m.tid;
		rear = (rear + 1) % 4;
		count++;
	}
	
	public synchronized Message<Integer> extract() {
		Message<Integer> m = new Message<Integer>();
		m.data = (Integer) buffer[front].data;
		m.tid = buffer[front].tid;
		front = (front + 1) % 4;
		count--;
		return m;
	}
	
	public synchronized int how_many() {
		return count;
	}
}
