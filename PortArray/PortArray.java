package PortArray;
import SynchPort.*;
import java.util.*;
import FairSem.*;
import Message.*;

public class PortArray<T> {
	List <SynchPort <T>> PortArray;
	PortQueue ports_available; // FIFO queue of the semaphores available
	FairSem wait; 
	int dim;
	
	public PortArray(int n, int Nsenders) {
		PortArray = new ArrayList<SynchPort<T>>();	
		wait = new FairSem(0, 1, false);
		ports_available = new PortQueue(n);

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
		int i = 0, index = -1, indexQ;
		boolean first = false;
		
		if (ports_available.mode_extraction(v, n) && !ports_available.empty()) 
			index = ports_available.removeFIFO();	
		else if ((indexQ = ports_available.find(v,n)) != -1 && !ports_available.empty())
		          index = ports_available.remove_x(indexQ); 
		else {
			while (i < n) {
				if (PortArray.get(v[i]).statePort() == true) {
					if (!first) {
						index = i;
						first = true;
					}
					else  ports_available.insert(i);					
				}					
				i++;
			}
		}
		
		return index;
	}
	
	public Message<T> receive(int v[], int n) {
		int index;
		Message<T> m;
	
		while ((index = checkPorts(v, n)) == -1) 
			wait.P();
			
		m = PortArray.get(v[index]).receive();
		m.index = index;
		
		return m;
	}
	
}
