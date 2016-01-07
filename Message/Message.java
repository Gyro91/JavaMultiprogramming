package Message;
import SynchPort.*;

public class Message<T> {
	public T data; // It contains the information of the message
	public SynchPort <T> response; // Port where the sender could wait for the response
	public long tid; // Tid of the sender process
	public int index; // Used to save the index of the Port after a receive in PortArray
	public int priority; // Priority of the thread (0<=p<=10)
}
