package PortArray;
import SynchPort.*;
import java.util.*;
import FairSem.*;
import Message.*;


public class PortArray<T> {
	List <SynchPort <T>> PortArray;
	FIFO_Queue sem_available; // FIFO queue of the semaphores available
	FairSem wait, mutex; 
	int dim;
	
	public PortArray(int n, int Nsenders) {
		PortArray = new ArrayList<SynchPort<T>>();	
		wait = new FairSem(0, n, false);
		sem_available = new FIFO_Queue(n);

		dim = n;		
		for (int i=0; i<n; i++) {
			SynchPort<T> sp = new SynchPort<T>(Nsenders);
			PortArray.add(sp);
		}
	}
	
	public void send(Message<T> m, int p) {
		if (p < 0 || p >= dim) {
			System.out.println("Error: Port not found");
			System.exit(1);
		}		
		
		wait.V();
		PortArray.get(p).send(m);
	}

	public int checkPorts(int v[], int n) {
		int i = 0, index = -1;
		
		if(!sem_available.empty()) {
			index = sem_available.remove();
		}
		else {
			while (i < n) {
				if (PortArray.get(v[i]).statePort() == true) 
					sem_available.insert(i);
			i++;
			}
			index = sem_available.remove();
		}
		return index;
	}
	
	public Message<T> receive(int v[], int n) {
		int index;
		Message<T> m;
		
		// An if is sufficient instead a 
		// while because fairSem is implemented 
		// with passing the baton technique
		// it's used a while for getting the correct index after a wake-up
		
		while ((index = checkPorts(v, n)) == -1)
			wait.P();
		m = PortArray.get(v[(int) index]).receive();
		m.index = (int) index;
		
		return m;
	}
	
}
