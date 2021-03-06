import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread{
	Main main;
	//Node sourceNode;
	Node destinationNode;
	Socket s;
	ObjectOutputStream oos;
	//ObjectInputStream ois;
	DataOutputStream out;
	BufferedReader br;
	public volatile static boolean startExecution = false;
	
	 public Client(Node destinationNode, Main main)
	{
		this.main = main;
		//this.sourceNode = sourceNode;
		this.destinationNode = destinationNode;
		
		try {
			s = new Socket(destinationNode.getHostname(), destinationNode.getPortNumber());
			//oos = new ObjectOutputStream(s.getOutputStream());
			out = new DataOutputStream(s.getOutputStream());
			System.out.println("Client started "+ this.main.node.getId());
			
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
		//send.setDestinationNode(destinationNode);
		
		String message = new String();
		if(send.getMessage().equalsIgnoreCase("hello"))
		{
			message = send.getMessage()+" "+send.getSourceNode().getId();
		}
		else
		{
			message = send.getMessage() + " " + send.getSourceNode().getId() + " "+ send.getDestinationNode().getId() + " "+send.getSourceNode().getTimestamp() +" " + send.getSourceNode().getRequestTimestamp();
		}
		
		 //System.out.println(send.getMessage() + " " + send.getSourceNode().getId() + " "+ send.getDestinationNode().getId());
		 try {
			//oos.reset();
			//oos.writeObject(send);

			//oos.flush();
			// System.out.println("Message" + message);
			 out.writeBytes(message);
			 out.writeBytes("\n");
			 out.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	
	 public void run()
	 {
		 try {
			//ois = new ObjectInputStream(s.getInputStream());
			 br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// System.out.println("Client started "+ main.node.getId() + "for" + destinationNode.getId());
		 while(true)
		 {
			 
			 try {
				//Message m = (Message)ois.readObject();

				 String message = br.readLine();
				 
				 String[] split = message.split("\\s+");
				 Message m = new Message();
				 Node source = Main.hostNameHM.get(Integer.parseInt(split[1]));
				 Node destination = Main.hostNameHM.get(Integer.parseInt(split[2]));
				 source.setTimestamp(Integer.parseInt(split[3]));
				 
				 source.setRequestTimestamp(Integer.parseInt(split[4]));
				 m.setMessage(split[0]);
				 m.setSourceNode(source);
				 m.setDestinationNode(destination);

				 if(m.getMessage().equalsIgnoreCase("request"))
					{
						main.request(m);
					}
					else if(m.getMessage().equalsIgnoreCase("release"))
					{
						main.release(m);
					}
					else if(m.getMessage().equalsIgnoreCase("grant"))
					{
						main.grant(m);
					}
					else if(m.getMessage().equalsIgnoreCase("yield"))
					{
						main.yield(m);
					}
					else if(m.getMessage().equalsIgnoreCase("inquire"))
					{
						main.inquire(m);
					}
					else if(m.getMessage().equalsIgnoreCase("failed"))
					{
						main.failed(m);
					}
					else
					{
						startExecution = true;
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	 }
	 
	
	 
}
