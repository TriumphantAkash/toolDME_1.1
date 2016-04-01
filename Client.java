import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Client extends Thread{
	Main main;
	//Node sourceNode;
	Node destinationNode;
	Socket s;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	 public Client(Node destinationNode, Main main)
	{
		 
		this.main = main;
		//this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		
		try {
			s = new Socket(destinationNode.getHostname(), destinationNode.getPortNumber());
			oos = new ObjectOutputStream(s.getOutputStream());
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	 
	 public synchronized void sendMessage(Message send)
	 {
		 //System.out.println("Client request time stamp" + main.node.getRequestTimestamp());
		 send.setDestinationNode(destinationNode);
		 //System.out.println(send.getMessage() + " " + send.getSourceNode().getId() + " "+ send.getDestinationNode().getId());
		 try {
			//oos.reset();
			//oos.writeObject(send);
			oos.writeUnshared(send);
			oos.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	
	 public void run()
	 {
		 try {
			ois = new ObjectInputStream(s.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// System.out.println("Client started "+ main.node.getId() + "for" + destinationNode.getId());
		 while(true)
		 {
			 
			 try {
				//Message m = (Message)ois.readObject();
				 Message m = (Message)ois.readUnshared();
				
				if(m.getMessage().equalsIgnoreCase("grant"))
				{
					main.grant(m);
				}
				else if(m.getMessage().equalsIgnoreCase("inquire"))
				{
					main.inquire(m);
				}
				else if(m.getMessage().equalsIgnoreCase("failed"))
				{
					main.failed(m);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 }
	 
	 public ArrayList<Node> removeElementFromList(ArrayList<Node> al, Integer id)
		{
			ArrayList<Node> alm = new ArrayList<Node>();
			for(Node n : al)
			{
				if(n.getId()==id)
				{
					continue;
				}
				else
					alm.add(n);
			}
			
			return alm;
		}
	 
}
