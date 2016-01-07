package PortArray;
import SynchPort.*;
import java.util.*;
import FairSem.*;
import Message.*;

public class PortArray<T> {
	List <SynchPort <T>> PortArray;
	FairSem wait;
	int dim;
	
	public PortArray(int n, int Nsenders) {
		PortArray = new ArrayList<SynchPort<T>>();	
		wait = new FairSem(0, n, false);
		
		dim = n;		
		for(int i=0; i<n; i++) {
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
		boolean end = false;
		int i = 0, index = -1;
		
		while (i < n && !end) {
			if (PortArray.get(v[i]).statePort() == true) {
				end = true;
				index = v[i];
			}
			i++;
		}
		return index;
	}
	
	public Message<T> receive(int v[], int n) {
		int index;
		Message<T> m;
		
		// An if is sufficient instead a 
		// while because fairSem is implemented 
		// with passing the baton technique
		
		while ((index = checkPorts(v, n)) == -1)
			wait.P();
		System.out.println("Awakened");
		m = PortArray.get(v[index]).receive();
		m.index = index;
		
		return m;
	}
	
}
