import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SocketConnectionServer extends Thread{

	Node node ;
	public static HashMap<Integer,ObjectOutputStream> clientOS;

	public SocketConnectionServer(Node node)
	{
		this.node = node;
	}

	public void run()
	{
		go();
	}

	public synchronized void go()
	{
		try{
			ServerSocket serverSock = new ServerSocket(node.getPortNumber());
			System.out.println("Server started " + node.getId());
			Socket sock;
			MinHeap mn = new MinHeap();
			while(true)
			{
				sock = serverSock.accept();

				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				Message m = (Message) (ois.readObject());
				ois.close();

				//set logical clock
//				if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
//					node.setTimestamp(m.getSourceNode().getTimestamp()+1);
//				}else {
//					node.setTimestamp(node.getTimestamp()+1);
//				}
				synchronized(this)
				{
					if(m.getMessage().equalsIgnoreCase("request"))
					{
						System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
						System.out.println("Node "+ node.getId() + " PQ size"+ node.getQueue().size());
						if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
							node.setTimestamp(m.getSourceNode().getTimestamp()+1);
						}else {
							node.setTimestamp(node.getTimestamp()+1);
						}
						if(!node.isGrantFlag())
						{	//if this is first request, grant Flag is false
							node.getQueue().add(m.getSourceNode());
							System.out.println("Node "+ node.getId() + " PQ first"+ node.getQueue().get(0).getId() + " TS "+ node.getQueue().get(0).getRequestTimestamp());
							
							//send grant to the source of m
							node.setTimestamp(node.getTimestamp()+1);
							ArrayList<Message> alm = new ArrayList<Message>();
							Message grantMsg = new Message();
							grantMsg.setSourceNode(node);
							grantMsg.setDestinationNode(m.getSourceNode());
							grantMsg.setMessage("grant");
							node.setGrantFlag(true);
							node.setGrantOwner(m.getSourceNode());
							alm.add(grantMsg);
							SocketConnectionClient scc = new SocketConnectionClient(alm);
							scc.start();						
						}
						else 
						{
							//if(m.getSourceNode().getTimestamp() > node.getGrantOwner().getTimestamp())
							if(node.getQueue().size()>0)
							{
								System.out.println("Else");
								System.out.println("Node "+ node.getId() + " PQ first "+ node.getQueue().get(0).getId() + " TS "+ node.getQueue().get(0).getRequestTimestamp());
								System.out.println("Node "+ node.getId() + " Src "+ m.getSourceNode().getId() + " TS "+ m.getSourceNode().getRequestTimestamp());
								if((m.getSourceNode().getRequestTimestamp() > node.getQueue().get(0).getRequestTimestamp()) || ((m.getSourceNode().getRequestTimestamp()==node.getQueue().get(0).getRequestTimestamp()) && m.getSourceNode().getId()>node.getQueue().get(0).getId()))
								{	//m's timestamp is more than grant owner's timestamp
									//put this req m into the original priority queue
							
									node.getQueue().add(m.getSourceNode());
									
									System.out.println("Before build heap");
									System.out.print("Node "+node.getId() + " PQ ");
									for(Node n : node.getQueue())
									{
										System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
									}
									System.out.println();
									mn.buildMinHeap(node.getQueue());
									
									System.out.println("After build heap");
									System.out.print("Node "+node.getId() + " PQ ");
									for(Node n : node.getQueue())
									{
										System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
									}
									System.out.println();
									//ArrayList<Message> alm = new ArrayList<Message>();
									Message sendFailed = new Message();
									sendFailed.setSourceNode(node);
									sendFailed.setDestinationNode(m.getSourceNode());
									sendFailed.setMessage("failed");
									//alm.add(sendFailed);
									SocketConnectionClient scc = new SocketConnectionClient(sendFailed);
									scc.start();		
								}else
								{
									node.getWaitingForYield().add(m.getSourceNode());	//add this req to waitingForYield list
									if(!node.isInquireFlag()) 
									{//check inquire Flag so that don't send inquire to a node again and again
										//send inquire to grant owner
										node.setTimestamp(node.getTimestamp()+1);
										ArrayList<Message> alm = new ArrayList<Message>();
										Message inquireMsg = new Message();
										inquireMsg.setSourceNode(node);
										inquireMsg.setDestinationNode(node.getGrantOwner());
										inquireMsg.setMessage("inquire");
										node.setInquireFlag(true);
										alm.add(inquireMsg);
										SocketConnectionClient scc = new SocketConnectionClient(alm);
										scc.start();		
									} 
								}
							}
							
						}

					}
					else if(m.getMessage().equalsIgnoreCase("release"))
					{
						System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
						for(int i=0;i<node.vectorClock.length;i++)
						{
							if(m.vectorClock[i]>node.vectorClock[i])
							{
								node.vectorClock[i] = m.vectorClock[i];
							}
						}
						if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
							node.setTimestamp(m.getSourceNode().getTimestamp()+1);
						}else {
							node.setTimestamp(node.getTimestamp()+1);
						}
						//1) delete first element from the main queue
						node.getQueue().remove(0);
						//mn.minHeapify(node.getQueue(), 0);
						node.setGrantFlag(false);
						
						//2) Add waitingForYield list to original queue
						for(Node n:node.getWaitingForYield())
						{
							node.getQueue().add(n);
						}
						
						node.setWaitingForYield(new ArrayList<Node>());
						if(node.getQueue().size()>0)
						{
							System.out.println("Before build heap");
							System.out.print("Node "+node.getId() + " PQ ");
							for(Node n : node.getQueue())
							{
								System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
							}
							System.out.println();
							
							mn.buildMinHeap(node.getQueue());
							
							System.out.println("After build heap");
							System.out.print("Node "+node.getId() + " PQ ");
							for(Node n : node.getQueue())
							{
								System.out.print("("+n.getRequestTimestamp()+","+n.getId()+",");
							}
							System.out.println();
							
							node.setGrantOwner(node.getQueue().get(0));
							ArrayList<Message> alm = new ArrayList<Message>();
							Message sendGrant = new Message();
							sendGrant.setMessage("grant");
							sendGrant.setSourceNode(node);
							sendGrant.setDestinationNode(node.getQueue().get(0));
							node.setGrantFlag(true);
							alm.add(sendGrant);
							SocketConnectionClient scc = new SocketConnectionClient(alm);
							scc.start();

						}
					}

					
					else if(m.getMessage().equalsIgnoreCase("yield"))
					{
						System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
						if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
							node.setTimestamp(m.getSourceNode().getTimestamp()+1);
						}else {
							node.setTimestamp(node.getTimestamp()+1);
						}
						if(node.getWaitingForYield().size()>0)
						{
							for(Node n : node.getWaitingForYield())
							{
								node.getQueue().add(n);
							}
							mn.buildMinHeap(node.getQueue());
							node.setTimestamp(node.getTimestamp()+1);
							ArrayList<Message> alm = new ArrayList<Message>();
							Message send = new Message();
							send.setSourceNode(node);
							send.setDestinationNode(node.getQueue().get(0));
							send.setMessage("grant");
							System.out.println("Node "+node.getId()+" inside yield first of pq "+node.getQueue().get(0).getId());
							node.setWaitingForYield(new ArrayList<Node>());
							alm.add(send);
							SocketConnectionClient c = new SocketConnectionClient(alm);
							c.start();	

						}

					}
					
					else if(m.getMessage().equalsIgnoreCase("csenter"))
					{
						Main.ackFromResourceForCSEnter = true;
					}
					else if(m.getMessage().equalsIgnoreCase("csexit"))
					{
						Main.ackFromResourceForCSExit = true;
					}
				}
				
			}

		}
		catch(IOException ex)
		{
			ex.printStackTrace();

		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			System.out.println(" ==== here ? == " + this.node.getId());
			e.printStackTrace();
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
