package quorum;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.sun.xml.internal.ws.resources.SenderMessages;

import mainPackage.Main;
import models.Message;
import models.Node;
import utilities.MinHeap;

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
					if(main.getNode().getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.getNode().setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
					}
					if(!main.getNode().isGrantFlag())
					{	//if this is first request, grant Flag is false
						main.getNode().getQueue().add(msg.getSourceNode());
						System.out.println("Node "+ main.getNode().getId() + " PQ first"+ main.getNode().getQueue().get(0).getId() + " TS "+ main.getNode().getQueue().get(0).getRequestTimestamp());
						
						//send grant to the source of msg
						main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
						Message grantMsg = new Message();
						grantMsg.setSourceNode(main.getNode());
						grantMsg.setDestinationNode(msg.getSourceNode());
						grantMsg.setMessage("grant");
						main.getNode().setGrantFlag(true);
						main.getNode().setGrantOwner(msg.getSourceNode());						
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
						if(main.getNode().getQueue().size()>0)
						{
							System.out.println("Else");
							System.out.println("Node "+ main.getNode().getId() + " PQ first "+ main.getNode().getQueue().get(0).getId() + " TS "+ main.getNode().getQueue().get(0).getRequestTimestamp());
							System.out.println("Node "+ main.getNode().getId() + " Src "+ msg.getSourceNode().getId() + " TS "+ msg.getSourceNode().getRequestTimestamp());
							if((msg.getSourceNode().getRequestTimestamp() > main.getNode().getQueue().get(0).getRequestTimestamp()) || ((msg.getSourceNode().getRequestTimestamp()==main.getNode().getQueue().get(0).getRequestTimestamp()) && msg.getSourceNode().getId()>main.getNode().getQueue().get(0).getId()))
							{	//msg's timestamp is more than grant owner's timestamp
								//put this req msg into the original priority queue
						
								main.getNode().getQueue().add(msg.getSourceNode());
								
								System.out.println("Before build heap");
								System.out.print("Node "+main.getNode().getId() + " PQ ");
								for(Node n : main.getNode().getQueue())
								{
									System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
								}
								System.out.println();
								mn.buildMinHeap(main.getNode().getQueue());
								
								System.out.println("After build heap");
								System.out.print("Node "+main.getNode().getId() + " PQ ");
								for(Node n : main.getNode().getQueue())
								{
									System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
								}
								System.out.println();
								//ArrayList<Message> alm = new ArrayList<Message>();
								Message sendFailed = new Message();
								sendFailed.setSourceNode(main.getNode());
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
								main.getNode().getWaitingForYield().put(msg.getSourceNode().getId(), msg.getSourceNode());	//add this req to waitingForYield list
								if(!main.getNode().isInquireFlag()) 
								{//check inquire Flag so that don't send inquire to a main.getNode() again and again
									//send inquire to grant owner
									main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
									Message inquireMsg = new Message();
									inquireMsg.setSourceNode(main.getNode());
									inquireMsg.setDestinationNode(main.getNode().getGrantOwner());
									inquireMsg.setMessage("inquire");
									main.getNode().setInquireFlag(true);
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
					if(main.getNode().getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.getNode().setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
					}
					//1) delete first element from the main queue
					main.getNode().getQueue().remove(0);
					//mn.minHeapify(main.getNode().getQueue(), 0);
					main.getNode().setGrantFlag(false);
					
					//2) Add waitingForYield list to original queue
					for(Entry<Integer, Node> n : main.getNode().getWaitingForYield().entrySet())
					{
						main.getNode().getQueue().add(n.getValue());
					}
					
					main.getNode().setWaitingForYield(new HashMap<Integer, Node>());
					if(main.getNode().getQueue().size()>0)
					{
						System.out.println("Before build heap");
						System.out.print("Node "+main.getNode().getId() + " PQ ");
						for(Node n : main.getNode().getQueue())
						{
							System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
						}
						System.out.println();
						
						mn.buildMinHeap(main.getNode().getQueue());
						
						System.out.println("After build heap");
						System.out.print("Node "+main.getNode().getId() + " PQ ");
						for(Node n : main.getNode().getQueue())
						{
							System.out.print("("+n.getRequestTimestamp()+","+n.getId()+",");
						}
						System.out.println();
						
						main.getNode().setGrantOwner(main.getNode().getQueue().get(0));
						Message sendGrant = new Message();
						sendGrant.setMessage("grant");
						sendGrant.setSourceNode(main.getNode());
						sendGrant.setDestinationNode(main.getNode().getQueue().get(0));
						main.getNode().setGrantFlag(true);
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
					if(main.getNode().getTimestamp() < msg.getSourceNode().getTimestamp()){
						main.getNode().setTimestamp(msg.getSourceNode().getTimestamp()+1);
					}else {
						main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
					}
					if(main.getNode().getWaitingForYield().size()>0)
					{
						for(Entry<Integer, Node> n : main.getNode().getWaitingForYield().entrySet())
						{
							main.getNode().getQueue().add(n.getValue());
						}
						mn.buildMinHeap(main.getNode().getQueue());
						main.getNode().setTimestamp(main.getNode().getTimestamp()+1);
						Message send = new Message();
						send.setSourceNode(main.getNode());
						send.setDestinationNode(main.getNode().getQueue().get(0));
						send.setMessage("grant");
						System.out.println("Node "+main.getNode().getId()+" inside yield first of pq "+main.getNode().getQueue().get(0).getId());
						main.getNode().setWaitingForYield(new HashMap<Integer, Node>());
						try {
							writeMessage(send);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}
				
				else if(msg.getMessage().equalsIgnoreCase("csenter"))
				{
					Main.ackFromResourceForCSEnter = true;
				}
				else if(msg.getMessage().equalsIgnoreCase("csexit"))
				{
					Main.ackFromResourceForCSExit = true;
				}
			}
			
			//2) listen to input stream
			//once a messgea is arrived it goes to the start if the loop and process the message again
		}
	}
	
	//used to send message to the client/(s)
	
	void writeMessage(Message am) throws UnknownHostException, IOException{
			ObjectOutputStream oos = SocketConnectionServer.clientOS.get(am.getDestinationNode().getId());
			oos.writeObject(am);
			//oos.close();
	}
}
