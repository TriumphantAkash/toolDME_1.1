

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
		

		while(true){
			//1) process the message
			synchronized(this)
			{
				if(msg.getMessage().equalsIgnoreCase("request"))
				{
					Message receive = main.request(msg);
					if(receive!=null)
					{
						try {
							writeMessage(receive);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				else if(msg.getMessage().equalsIgnoreCase("release"))
				{
					Message receive = main.release(msg);
					if(receive!=null)
					{
						try {
							writeMessage(receive);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				
				else if(msg.getMessage().equalsIgnoreCase("yield"))
				{
					Message receive = main.yield(msg);
					if(receive!=null)
					{
						try {
							writeMessage(receive);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				try {
					//this.msg = (Message)this.ois.readObject();
					this.msg = (Message)this.ois.readUnshared();
//					Message test = (Message)this.ois.readObject();
//					System.out.println("Nilesh " + test.getMessage() + " " + test.getSourceNode().getId() + " RTS " + test.getSourceNode().getRequestTimestamp());
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
	
	public synchronized void writeMessage(Message am) throws UnknownHostException, IOException{
		
			//ObjectOutputStream oos =
		//SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).reset();
		//SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeObject(am);
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeUnshared(am);
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).flush();
		
			//oos.writeObject(am);
			//oos.close();
	}
}
