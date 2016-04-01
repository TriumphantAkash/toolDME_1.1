import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Client extends Thread{
	Main main;
	Node sourceNode;
	Node destinationNode;
	Socket s;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	 public Client(Node sourceNode, Node destinationNode, Main main)
	{
		this.main = main;
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
	 
	 public void sendMessage(String message)
	 {
		 Message send = new Message();
		 send.setMessage(message);
		 send.setSourceNode(sourceNode);
		 send.setDestinationNode(destinationNode);
		 try {
			oos.writeObject(send);
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
					if(main.node.getTimestamp() < m.getSourceNode().getTimestamp()){
						main.node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
//					main.node.getGrant().add(m.getSourceNode());
					main.node.grant.put(m.getSourceNode().getId(), m.getSourceNode());
					System.out.print("Node "+ main.node.getId() + " Grant list ");
					for(Integer n: main.node.grant.keySet())
					{
						System.out.print("(" + main.node.grant.get(n) + "),");
					}
					System.out.println();
					
				
					//main.node.setFailedList(removeElementFromList(node.getFailedList(), m.getSourceNode().getId()));
					main.node.deleteFromFailedList(m.getSourceNode().getId());

					//2) check size of grantArrayList 
					if(main.node.grant.size() == main.node.getQuorum().size())
					{
						synchronized(this)
						{
							
							Main.csEnter = true;
							main.node.grant.clear();
							main.node.setRequestTimestamp(main.node.getTimestamp());
						}
						//go into critical section
					}


				}
				else if(m.getMessage().equalsIgnoreCase("inquire"))
				{
					System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
					if(main.node.getTimestamp() < m.getSourceNode().getTimestamp()){
						main.node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
					if(main.node.getFailedList().size()>0)
					{	
						
						main.node.deleteFromGrant(m.getSourceNode().getId());
						main.node.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
						//node.getInquireQuorum().add(m.getSourceNode());
						System.out.print("Inquire List ");
						for(Integer n: main.node.inquireQuorum.keySet())
						{
							System.out.print("(" + main.node.inquireQuorum.get(n) + ")");
						}
						System.out.println();
						
						main.node.setTimestamp(main.node.getTimestamp()+1);
						
						for(Integer n:main.node.inquireQuorum.keySet())
						{
							Main.clientThread.get(n).sendMessage("yield");
							/*Message m1 = new Message();
							m1.setDestinationNode(main.node.inquireQuorum.get(n));
							m1.setSourceNode(main.node);
							m1.setMessage("yield");
							sendMessage(m1);*/
						}
						
						main.node.inquireQuorum.clear();
	
					}
					else
					{
						main.node.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
						System.out.println("else inquire");
						System.out.print("Node "+ main.node.getId() +"Inquire List ");
						for(Integer n:main.node.inquireQuorum.keySet())
						{
							System.out.print("(" + main.node.inquireQuorum.get(n) + ")");
						}
						System.out.println();
					}

				}
				else if(m.getMessage().equalsIgnoreCase("failed"))
				{
					System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
					if(main.node.getTimestamp() < m.getSourceNode().getTimestamp()){
						main.node.setTimestamp(m.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
					//node.getFailedList().add(m.getSourceNode());
					main.node.failedList.put(m.getSourceNode().getId(), m.getSourceNode());
					System.out.println("Node "+main.node.getId() + " inside failed : inqQuorum size "+ main.node.getInquireQuorum().size());
					if(main.node.getInquireQuorum().size()>0)
					{
						ArrayList<Message> alm = new ArrayList<Message>();
						//for(Node n: node.getInquireQuorum())
						for(Integer n : main.node.inquireQuorum.keySet())
						{
							System.out.println("Node "+main.node.getId() + " inside failed inq quorum : "+ main.node.inquireQuorum.get(n));
							Main.clientThread.get(n).sendMessage("yield");
							/*Message send = new Message();
							main.node.setTimestamp(main.node.getTimestamp()+1);
							send.setDestinationNode(destinationNode);
							send.setSourceNode(sourceNode);
							send.setMessage("yield");
							sendMessage(send);*/
							
							main.node.deleteFromGrant(n);
							//node.setGrant(removeElementFromList(node.getGrant(), n.getId()));

													
							//alm.add(send);
							System.out.print("Node " + main.node.getId() + " Grant list after deletion ");
							//for(Node a: node.getGrant())
							for(Integer a: main.node.grant.keySet())
							{
								System.out.print(main.node.grant.get(a).getId() + ",");
							}
							System.out.println();
							
						}
						System.out.println("Check alm Node "+ main.node.getId());
						main.node.inquireQuorum.clear();
						//node.setInquireQuorum(new ArrayList<Node>());
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
