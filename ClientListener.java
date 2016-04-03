

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class ClientListener extends Thread{
	
	//private ObjectInputStream ois;
	BufferedReader br;
	private String msg;
	private Main main;
	ClientListener(BufferedReader br, String msg, Main main){
		this.br = br;
		this.main = main;
		this.msg = msg;
		
	}
	public void run(){

		while(true){
			//1) process the message
			synchronized(this)
			{
				
				try {
					
						
						msg = br.readLine();
						writeMessage(msg);
					
					//this.msg = (Message)this.ois.readObject();

					
					
					/*
					 String[] split = message.split("\\s+");
					 
					 msg = new Message();
					 Node source = Main.hostNameHM.get(Integer.parseInt(split[1]));
					 Node destination = Main.hostNameHM.get(Integer.parseInt(split[2]));
					 source.setTimestamp(Integer.parseInt(split[3]));
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
					 msg.setDestinationNode(destination);
*/
//					Message test = (Message)this.ois.readObject();
//					System.out.println("Nilesh " + test.getMessage() + " " + test.getSourceNode().getId() + " RTS " + test.getSourceNode().getRequestTimestamp());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			//2) listen to input stream
			//once a messgea is arrived it goes to the start if the loop and process the message again
		}
	}
	
	//used to send message to the client/(s)
	
	public synchronized void writeMessage(String am) throws UnknownHostException, IOException{
		
		//String message = new String();
		//message = am.getMessage() + " " + am.getSourceNode().getId() + " "+ am.getDestinationNode().getId() + " "+main.node.getTimestamp();
		
		String[] split = am.split("\\s+");
		int sendNode = Integer.parseInt(split[2]);
			//ObjectOutputStream oos =
		//SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).reset();

		/*SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeObject(am);
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).flush();
*/
		/*SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeBytes(message);
		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).writeBytes("\n");

		SocketConnectionServer.clientOS.get(am.getDestinationNode().getId()).flush();*/
		
		SocketConnectionServer.clientOS.get(sendNode).writeBytes(am);
		SocketConnectionServer.clientOS.get(sendNode).writeBytes("\n");

		SocketConnectionServer.clientOS.get(sendNode).flush();
			//oos.writeObject(am);
			//oos.close();
	}
}
