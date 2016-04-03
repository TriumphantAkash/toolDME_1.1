
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class SocketConnectionServer extends Thread{

	Main m ;
	Node node;
	public volatile static HashMap<Integer,DataOutputStream> clientOS;

	private static ServerSocket serverSock;
	public SocketConnectionServer(Node node,Main m)
	{
		this.m = m;
		this.clientOS = new HashMap<Integer, DataOutputStream>();
		this.node = node;
	}

	public void run()
	{
		go();
	}

	public synchronized void go()
	{
		try{
			/*serverSock = new ServerSocket(m.getNode().getPortNumber());
			System.out.println("Server started " + m.getNode().getId());*/
			serverSock = new ServerSocket(node.getPortNumber());
			System.out.println("Server started " + node.getId());
			Socket sock;
			while(true)
			{
				sock = serverSock.accept();

				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

				String message = br.readLine();
				String[] split = message.split("\\s+");
				 
				 
				 /*
				 Message msg = new Message();
				 Node source = new Node(); 
				 source = Main.hostNameHM.get(Integer.parseInt(split[1]));
				 Node destination = Main.hostNameHM.get(Integer.parseInt(split[2]));
				 source.setTimestamp(timestamp);
				 if(split[0].equalsIgnoreCase("request"))
				 {
					 Main.requestTimeStamp.put(Integer.parseInt(split[1]),Integer.parseInt(split[4]));
					 source.setRequestTimestamp(Integer.parseInt(split[4]));
				 }
				 else
				 {
					 source.setRequestTimestamp(Main.requestTimeStamp.get(Integer.parseInt(split[1])));
				 }
				 
				 msg.setMessage(split[0]);
				 msg.setSourceNode(source);
				 msg.setDestinationNode(destination);*/
				
				
				
				clientOS.put(Integer.parseInt(split[1]), out);
				
				ClientListener clientListener = new ClientListener(br, message, m);
				clientListener.start();
				//ois.close();
				
			}

		}
		catch(IOException ex)
		{
			ex.printStackTrace();

		}
		catch (Exception e) {
			System.out.println(" ==== here ? == " + m.getNode().getId());
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
