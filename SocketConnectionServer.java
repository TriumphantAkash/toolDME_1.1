
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class SocketConnectionServer extends Thread{

	Main m ;
	public volatile static HashMap<Integer,ObjectOutputStream> clientOS;

	private static ServerSocket serverSock;
	public SocketConnectionServer(Main m)
	{
		this.m = m;
		this.clientOS = new HashMap<Integer, ObjectOutputStream>();
	}

	public void run()
	{
		go();
	}

	public synchronized void go()
	{
		try{
			serverSock = new ServerSocket(m.getNode().getPortNumber());
			System.out.println("Server started " + m.getNode().getId());
			Socket sock;
			while(true)
			{
				sock = serverSock.accept();

				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				//Message msg = (Message) (ois.readObject());
				Message msg = (Message) (ois.readUnshared());
				clientOS.put(msg.getSourceNode().getId(), oos);
				
				ClientListener clientListener = new ClientListener(ois, msg, m);
				clientListener.start();
				//ois.close();
				
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
