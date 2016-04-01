import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceProcess {
	public static boolean resourceUseFlag = false;
	int totalNode;
	public static boolean dmeHolds = true;
	int numberOfRequest;
	static int counter = 0;
	//port number passed as command line argument
	public static ArrayList<Integer> csEnterList = new ArrayList<Integer>();

	public static void main(String[] args) throws IOException {

		ResourceProcess rp = new ResourceProcess();
		File f = new File(args[1]);
		rp.readConfigFile(f);


		ServerSocket serverSock;
		try {
			serverSock = new ServerSocket(Integer.parseInt(args[0]));
			int totalRequest = (rp.totalNode*rp.numberOfRequest);
			//int totalRequest = 12;

			System.out.println("Resource Server started at port: "+args[0]);

			while(true)
			{
				Socket sock = serverSock.accept();
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				Message m = (Message) (ois.readObject());
				ois.close();

				if(m.getMessage().equalsIgnoreCase("csenter")){
					counter++;
					csEnterList.add(m.getSourceNode().getId());

					if(resourceUseFlag == true){

						//Mutual exclusion doesn't holds
						//print no here
						if(dmeHolds == true){

							System.out.println("[DME RESULT] \"DME Doesnt Hold\" [DME RESULT]");
							dmeHolds = false;
						}

					}else {

						resourceUseFlag = true;
					}
					Message am = new Message();
					am.setMessage("csenter");
					am.setDestinationNode(m.getSourceNode());

					//SocketConnectionClient c = new SocketConnectionClient(am);
					//c.start();
					
					rp.sentMessage(am);
				}

				else if(m.getMessage().equalsIgnoreCase("csexit")){

					if(csEnterList.contains(m.getSourceNode().getId()))
					{
						//remove this node from th arraylist
						csEnterList = rp.removeElementFromList(csEnterList, m.getSourceNode().getId());
						if(csEnterList.isEmpty())
						{

							resourceUseFlag = false;
						}	
						System.out.println("Counter" + counter);
						if(counter==(totalRequest) && dmeHolds)
						{
							System.out.println("[DME RESULT] \"DME Hold\" [DME RESULT]");
						}
					}
					Message am = new Message();
					am.setMessage("csexit");
					am.setDestinationNode(m.getSourceNode());

					//SocketConnectionClient c = new SocketConnectionClient(am);
					//c.start();
					
					rp.sentMessage(am);
				}
				//either cs enter or cs exit
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readConfigFile(File f)
	{
		FileReader fileReader;
		try {
			fileReader = new FileReader(f);
			BufferedReader br = new BufferedReader(fileReader);
			String line1 = br.readLine();
			String[] words = line1.split("\\s+");
			totalNode = Integer.parseInt(words[0]);
			numberOfRequest = Integer.parseInt(words[3]);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayList<Integer> removeElementFromList(ArrayList<Integer> al, Integer id)
	{
		ArrayList<Integer> alm = new ArrayList<Integer>();
		for(Integer n : al)
		{
			if(n==id)
			{
				continue;
			}
			else
				alm.add(n);
		}

		return alm;
	}

	public void sentMessage(Message m)
	{
		try 
		{

			Socket client = new Socket(m.getDestinationNode().getHostname(), m.getDestinationNode().getPortNumber());
			ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
			oos.writeObject(m);
			oos.close();
			client.close();



		}
		catch(Exception e)
		{

			System.out.println(m.getMessage()+" - " + m.getSourceNode().getId() + " - " + m.getDestinationNode().getId());

			e.printStackTrace();
		}
	}
}
