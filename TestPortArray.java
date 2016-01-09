import PortArray.*;
import Message.*;

class Receiver_T extends Thread {
	
	private int v1[]; // Set of ports with indexes 0,1
	private int v2[]; // Set of ports with indexes 2,3
	private int n;
	public static PortArray <Integer> pa = new PortArray<Integer>(4, 2);
	
	public Receiver_T() {
		v1 = new int[2];
		v2 = new int[2];
		for (int i=0; i<2; i++) {
			v1[i] = i;
			v2[i] = i + 2;
		}
		n = 2;
	}
	
	public void run() {
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		Message<Integer> m = pa.receive(v1, n);
		System.out.println("Receiver: value = " + m.data);
		
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m = pa.receive(v2, n);
		
		System.out.println("Receiver: value = " + m.data);
		
		m = pa.receive(v1, n);
		
		System.out.println("Receiver: value = " + m.data);
	}
}

class Sender_T1 extends Thread {
	private int data;
	private int port;
	
	public Sender_T1(int info, int pt) {
		data = info;
		port = pt;
	}
	
	public void run() { 
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message<Integer> m = new Message<Integer>();
		m.data = data;
			
		Receiver_T.pa.send(m, port);
		
		m.data = 3;
		
		Receiver_T.pa.send(m, port + 1);
	
	}
}

class Sender_T2 extends Thread {
	private int data;
	private int port;
	
	public Sender_T2(int info, int pt) {
		data = info;
		port = pt;
	}
	
	public void run() { 
		
		Message<Integer> m = new Message<Integer>();
		m.data = data;
		
	
		Receiver_T.pa.send(m, port);
		
	}
}



public class TestPortArray {
	
	public static void main(String[] args) {
		Receiver_T rec = new Receiver_T();
		Sender_T1 sender1 = new Sender_T1(1, 0);
		Sender_T2 sender2 = new Sender_T2(2, 2);
		
		rec.start();
		sender1.start();
		sender2.start();
	}

}
