import PortArray.*;
import Message.*;

class Receiver_T extends Thread {
	
	int v[];
	int n;
	
	public Receiver_T() {
		v = new int[4];
		for (int i=0;i<4; i++)
			v[i] = i;
		n = 4;
	}
	public static PortArray <Integer> pa = new PortArray<Integer>(4, 1);
	
	public void run() {
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message<Integer> m = pa.receive(v, n);
		
		System.out.println("Receiver: value = " + m.data);
	}
}

class Sender_T extends Thread {
	
	public void run() {
		
		Message<Integer> m = new Message<Integer>();
		m.data = 3;
	
		Receiver_T.pa.send(m, 2);
		
		System.out.println("Sender: Forwarded " + m.data);
	}
}


public class TestPortArray {
	
	public static void main(String[] args) {
		Receiver_T rec = new Receiver_T();
		Sender_T send = new Sender_T();
		
		rec.start();
		send.start();
	}

}
