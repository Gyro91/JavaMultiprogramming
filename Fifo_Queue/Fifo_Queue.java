package Fifo_Queue;

// This class implements a circular array

public class Fifo_Queue {
	public int rear, front;
	public int dim;
	public int	count;     	// Number of threads blocked on the Queue
	public long threads[];  	// Queue of threads, each element contains
								// the id of the blocked thread.
	public Fifo_Queue(int n) {
		dim = n;
		threads = new long[n];
		count = 0;
		rear = front = 0;
	}
	
	public synchronized void insert(long tid) {
		threads[rear] = tid;
		count++;
		rear = (rear + 1) % dim;
	}
	
	public synchronized long remove() {
		long tid = threads[front];
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
	
	public synchronized long first() {
		if (count>0)
			return threads[front];
		else
			return -2;
	}
}
