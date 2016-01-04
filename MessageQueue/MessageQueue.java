package MessageQueue;
import Message.*;

class Message_T {
	int data;
	long tid;
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
	
	public void insert(Message<Integer> m) {	
		buffer[rear].data = (int) m.data;
		buffer[rear].tid =  (long) m.tid;
		rear = (rear + 1) % 4;
		count++;	
	}
	
	public Message<Integer> extract() {
		Message<Integer> m = new Message<Integer>();
				
		m.data = buffer[front].data;
		m.tid = buffer[front].tid;
		front = (front + 1) % 4;
		count--;		
		
		return m;
		
	}
	
	public int how_many() {
		return count;
	}
}
