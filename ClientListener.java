

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class ClientListener extends Thread{
	
	private ObjectInputStream ois;
	private Message msg;
	private Main main;
	ClientListener(ObjectInputStream ois, Message msg, Main main){
		this.ois = ois;
		this.main = main;
		this.msg = msg;
	}
	public void run(){
		

		MinHeap mn = new MinHeap();
		while(true){
			//1) process the message
			synchronized(this)
			{
				if(msg.getMessage().equalsIgnoreCase("request"))
				{
					System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
					if(main.node.getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
					if(!main.node.isGrantFlag())
					{	//if this is first request, grant Flag is false
						main.node.getQueue().add(msg.getSourceNode());
						System.out.println("Node "+ main.node.getId() + " PQ first"+ main.node.getQueue().get(0).getId() + " RTS "+ main.node.getQueue().get(0).getRequestTimestamp() + " TS " +main.node.getQueue().get(0).getTimestamp());
//						System.out.println("printing source");
//						System.out.println("Node "+ main.node.getId() + " PQ first"+ msg.getSourceNode().getId() + " RTS "+ msg.getSourceNode().getRequestTimestamp() + " TS " +msg.getSourceNode().getTimestamp());
						
						//send grant to the source of msg
						main.node.setTimestamp(main.node.getTimestamp()+1);
						Message grantMsg = new Message();
						grantMsg.setSourceNode(main.node);
						grantMsg.setDestinationNode(msg.getSourceNode());
						grantMsg.setMessage("grant");
						main.node.setGrantFlag(true);
						main.node.setGrantOwner(msg.getSourceNode());						
						try {
							writeMessage(grantMsg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
												
					}
					else 
					{
						//if(msg.getSourceNode().getTimestamp() > node.getGrantOwner().getTimestamp())
						if(main.node.getQueue().size()>0)
						{
							System.out.println("Else");
							System.out.println("Node "+ main.node.getId() + " PQ first "+ main.node.getQueue().get(0).getId() + " TS "+ main.node.getQueue().get(0).getRequestTimestamp());
							System.out.println("Node "+ main.node.getId() + " Src "+ msg.getSourceNode().getId() + " TS "+ msg.getSourceNode().getRequestTimestamp());
							if((msg.getSourceNode().getRequestTimestamp() > main.node.getQueue().get(0).getRequestTimestamp()) || ((msg.getSourceNode().getRequestTimestamp()==main.node.getQueue().get(0).getRequestTimestamp()) && msg.getSourceNode().getId()>main.node.getQueue().get(0).getId()))
							{	//msg's timestamp is more than grant owner's timestamp
								//put this req msg into the original priority queue
						
								main.node.getQueue().add(msg.getSourceNode());
								
								System.out.println("Before build heap");
								System.out.print("Node "+main.node.getId() + " PQ ");
								for(Node n : main.node.getQueue())
								{
									System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
								}
								System.out.println();
								mn.buildMinHeap(main.node.getQueue());
								
								System.out.println("After build heap");
								System.out.print("Node "+main.node.getId() + " PQ ");
								for(Node n : main.node.getQueue())
								{
									System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
								}
								System.out.println();
								//ArrayList<Message> alm = new ArrayList<Message>();
								Message sendFailed = new Message();
								sendFailed.setSourceNode(main.node);
								sendFailed.setDestinationNode(msg.getSourceNode());
								sendFailed.setMessage("failed");
								try {
									writeMessage(sendFailed);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
							}else
							{
								main.node.getWaitingForYield().put(msg.getSourceNode().getId(), msg.getSourceNode());	//add this req to waitingForYield list
								if(!main.node.isInquireFlag()) 
								{//check inquire Flag so that don't send inquire to a main.node again and again
									//send inquire to grant owner
									main.node.setTimestamp(main.node.getTimestamp()+1);
									Message inquireMsg = new Message();
									inquireMsg.setSourceNode(main.node);
									inquireMsg.setDestinationNode(main.node.getGrantOwner());
									inquireMsg.setMessage("inquire");
									main.node.setInquireFlag(true);
									try {
										writeMessage(inquireMsg);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}		
								} 
							}
						}
						
					}

				}
				else if(msg.getMessage().equalsIgnoreCase("release"))
				{
					System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
					if(main.node.getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
					//1) delete first element from the main queue
					main.node.getQueue().remove(0);
					System.out.println("HAHAHAHAHAHA");
					for(Node n : main.node.getQueue())
					{
						
						System.out.println(n.getId() + " " + n.getRequestTimestamp() + " " + n.getTimestamp());
					}
					//mn.minHeapify(main.node.getQueue(), 0);
					main.node.setGrantFlag(false);
					
					//2) Add waitingForYield list to original queue
					for(Entry<Integer, Node> n : main.node.getWaitingForYield().entrySet())
					{
						main.node.getQueue().add(n.getValue());
					}
					
					main.node.setWaitingForYield(new HashMap<Integer, Node>());
					if(main.node.getQueue().size()>0)
					{
						System.out.println("Before build heap");
						System.out.print("Node "+main.node.getId() + " PQ ");
						for(Node n : main.node.getQueue())
						{
							System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
						}
						System.out.println();
						
						mn.buildMinHeap(main.node.getQueue());
						
						System.out.println("After build heap");
						System.out.print("Node "+main.node.getId() + " PQ ");
						for(Node n : main.node.getQueue())
						{
							System.out.print("("+n.getRequestTimestamp()+","+n.getId()+",");
						}
						System.out.println();
						
						main.node.setGrantOwner(main.node.getQueue().get(0));
						Message sendGrant = new Message();
						sendGrant.setMessage("grant");
						sendGrant.setSourceNode(main.node);
						sendGrant.setDestinationNode(main.node.getQueue().get(0));
						main.node.setGrantFlag(true);
						try {
							writeMessage(sendGrant);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}

				
				else if(msg.getMessage().equalsIgnoreCase("yield"))
				{
					System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
					if(main.node.getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.node.setTimestamp(main.node.getTimestamp()+1);
					}
					if(main.node.getWaitingForYield().size()>0)
					{
						for(Entry<Integer, Node> n : main.node.getWaitingForYield().entrySet())
						{
							main.node.getQueue().add(n.getValue());
						}
						mn.buildMinHeap(main.node.getQueue());
						main.node.setTimestamp(main.node.getTimestamp()+1);
						Message send = new Message();
						send.setSourceNode(main.node);
						send.setDestinationNode(main.node.getQueue().get(0));
						send.setMessage("grant");
						System.out.println("Node "+main.node.getId()+" inside yield first of pq "+main.node.getQueue().get(0).getId());
						main.node.setWaitingForYield(new HashMap<Integer, Node>());
						try {
							writeMessage(send);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}
				try {
					this.msg = (Message)this.ois.readObject();
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			//2) listen to input stream
			//once a messgea is arrived it goes to the start if the loop and process the message again
		}
	}
	
	//used to send message to the client/(s)
	
	void writeMessage(Message am) throws UnknownHostException, IOException{
		
			//ObjectOutputStream oos = 
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeObject(am);
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).reset();
			//oos.writeObject(am);
			//oos.close();
	}
}
