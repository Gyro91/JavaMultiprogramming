package PortArray;
import java.util.ArrayList;
import java.util.List;

public class PortQueue {
	private int dim;
	private int	count;     
	private List <Integer> port_indexes;  
	
	public PortQueue(int n) {
		dim = n;
		port_indexes = new ArrayList<Integer>();
		count = 0;
	}
	
	public void insert(int x) {
		port_indexes.add(x);
		count++;
	}
	
	public int removeFIFO() {
		System.out.println("Remove FIFO");
		int p_index = port_indexes.remove(0);
		count--;
		return p_index;
	}
	
	public int find(int v[], int n) {
		
		if (count > 0) 
			for (int j=0; j<n; j++)
			for (int i=0; i<count; i++) 
				if (port_indexes.get(i) == v[j]) 
					return i;		
		return -1;
	}
	
	public int remove_x(int i) {
		System.out.println("Remove_x");
		int p_index = port_indexes.remove(i);
		count--;
		return p_index;
	}
	
	// Returns true if remove can be done in FIFO mode
	// Otherwise returns false
	public boolean mode_extraction(int v[], int n) {	
		
		if (count > 0)
			for (int i=0; i<n; i++)
				if (port_indexes.get(0) == v[i])
					return true;
		
		return false;
	}
	
	public boolean full() {
		return (count == dim);
	}
	
	public boolean empty() {
		return (count == 0);
	}
	
	public int first() {
		if (count > 0)
			return port_indexes.get(0);
		else
			return -1;
	}
}
