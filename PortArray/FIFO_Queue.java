package PortArray;

public class FIFO_Queue {
	public int rear, front;
	public int dim;
	public int	count;     	// Number of threads blocked on the Queue
	public int threads[];  	// Queue of threads, each element contains
								// the id of the blocked thread.
	public FIFO_Queue(int n) {
		dim = n;
		threads = new int[n];
		count = 0;
		rear = front = 0;
	}
	
	public synchronized void insert(int tid) {
		threads[rear] = tid;
		count++;
		rear = (rear + 1) % dim;
	}
	
	public synchronized int remove() {
		int tid = threads[front];
		count--;
		front = (front + 1) % dim;
		return tid;
	}
	
	public synchronized boolean full() {
		return (count == dim);
	}
	
	public synchronized boolean empty() {
		return (count == 0);
	}
	
	public synchronized int first() {
		if (count > 0)
			return threads[front];
		else
			return -1;
	}
}
