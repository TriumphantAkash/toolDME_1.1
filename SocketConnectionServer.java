
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class SocketConnectionServer extends Thread{

	Main m ;
	Node node;
	public volatile static HashMap<Integer,DataOutputStream> clientOS;
	public static BlockingQueue<String> b = null;
	private static ServerSocket serverSock;
	public static int counter =0;
	public SocketConnectionServer(Node node,Main m)
	{
		this.m = m;
		this.clientOS = new HashMap<Integer, DataOutputStream>();
		this.node = node;
		b = new ArrayBlockingQueue<String>(2000);
	}

	public void run()
	{
		go();
	}

	public synchronized void go()
	{
		try{
			
			serverSock = new ServerSocket(node.getPortNumber());
			System.out.println("Server started " + node.getId());
			Socket sock;
			while(true)
			{
				sock = serverSock.accept();
				counter++;
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String message = br.readLine();
				String[] split = message.split("\\s+");
				System.out.println("Mesasge " + split[0]);
				clientOS.put(Integer.parseInt(split[1]), out);
				
				ClientListener clientListener = new ClientListener(br, message, m);
				clientListener.start();
				//ois.close();
				if(m.totalNode == counter)
				{
					ClientListenerWriter clw = new ClientListenerWriter(b);
					clw.start();
					
				}
				
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
