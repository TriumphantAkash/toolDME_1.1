import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Client extends Thread{
	Main m;
	Node sourceNode;
	Node destinationNode;
	Socket s;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	 public Client(Node sourceNode, Node destinationNode, Main m)
	{
		this.m = m;
		this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		try {
			s = new Socket(destinationNode.getHostname(), destinationNode.getPortNumber());
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	 
	 public void sendMessage(Message m)
	 {
		 try {
			oos.writeObject(m);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	
	 public void run()
	 {
		 while(true)
		 {
			 try {
				Message m = (Message)ois.readObject();
				if(m.getMessage().equalsIgnoreCase("grant"))
				{
					//1) update grant arrayList
					System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
					if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
						node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						node.setTimestamp(node.getTimestamp()+1);
					}
					node.getGrant().add(m.getSourceNode());
					System.out.print("Node "+ node.getId() + " Grant list ");
					for(Node n: node.getGrant())
					{
						System.out.print("(" + n.getId() + "),");
					}
					System.out.println();
					
				
					node.setFailedList(removeElementFromList(node.getFailedList(), m.getSourceNode().getId()));
					

					//2) check size of grantArrayList 
					if(node.getGrant().size() == node.getQuorum().size())
					{
						synchronized(this)
						{
							
							Main.csEnter = true;
							node.setGrant(new ArrayList<Node>());
							node.setRequestTimestamp(node.getTimestamp());
						}
						//go into critical section
					}


				}
				else if(m.getMessage().equalsIgnoreCase("inquire"))
				{
					System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
					if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
						node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						node.setTimestamp(node.getTimestamp()+1);
					}
					if(node.getFailedList().size()>0)
					{	
						System.out.println("Nilesh");
						
						
						node.setGrant(removeElementFromList(node.getGrant(), m.getSourceNode().getId()));

						node.getInquireQuorum().add(m.getSourceNode());
						System.out.print("Inquire List ");
						for(Node n: node.getInquireQuorum())
						{
							System.out.print("(" + n.getId() + ")");
						}
						System.out.println();
						
						node.setTimestamp(node.getTimestamp()+1);
						ArrayList<Message>msgList = new ArrayList<Message>();
						for(Node n:node.getInquireQuorum())
						{
							Message m1 = new Message();
							m1.setDestinationNode(n);
							m1.setSourceNode(node);
							m1.setMessage("yield");
							msgList.add(m1);
						}
						
						node.setInquireQuorum(new ArrayList<Node>());
						SocketConnectionClient scc = new SocketConnectionClient(msgList);
						scc.start();
					}
					else
					{
						node.getInquireQuorum().add(m.getSourceNode());
						System.out.println("else inquire");
						System.out.print("Node "+ node.getId() +"Inquire List ");
						for(Node n: node.getInquireQuorum())
						{
							System.out.print("(" + n.getId() + ")");
						}
						System.out.println();
					}

				}
				else if(m.getMessage().equalsIgnoreCase("failed"))
				{
					System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
					if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
						node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						node.setTimestamp(node.getTimestamp()+1);
					}
					node.getFailedList().add(m.getSourceNode());
					System.out.println("Node "+node.getId() + " inside failed : inqQuorum size "+ node.getInquireQuorum().size());
					if(node.getInquireQuorum().size()>0)
					{
						ArrayList<Message> alm = new ArrayList<Message>();
						for(Node n: node.getInquireQuorum())
						{
							System.out.println("Node "+node.getId() + " inside failed inq quorum : "+ n.getId());
							Message send = new Message();
							node.setTimestamp(node.getTimestamp()+1);
							send.setDestinationNode(n);
							send.setSourceNode(node);
							send.setMessage("yield");
							SocketConnectionClient c = new SocketConnectionClient(send);
							c.start();
							
							node.setGrant(removeElementFromList(node.getGrant(), n.getId()));

													
							//alm.add(send);
							System.out.print("Node " + node.getId() + " Grant list after deletion ");
							for(Node a: node.getGrant())
							{
								System.out.print(a.getId() + ",");
							}
							System.out.println();
							
						}
						System.out.println("Check alm Node "+ node.getId());
						for(Message m2: alm)
						{
							System.out.println(m2.getMessage() + " " + m2.getSourceNode().getId() + " " + m2.getDestinationNode().getId());
						}
						node.setInquireQuorum(new ArrayList<Node>());
//						SocketConnectionClient c = new SocketConnectionClient(alm);
//						c.start();	
					}
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
