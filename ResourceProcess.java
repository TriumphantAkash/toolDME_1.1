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

import javax.swing.plaf.synth.SynthSpinnerUI;

public class ResourceProcess {
	public static boolean resourceUseFlag = false;
	int totalNode;
	public static boolean dmeHolds = true;
	int numberOfRequest;
	static int counter = 0;
	//port number passed as command line argument
	public static ArrayList<Integer> csEnterList = new ArrayList<Integer>();
	static ResourceProcess rp;
	static int totalRequest;

	public static void main(String[] args) throws IOException {

		rp = new ResourceProcess();
		File f = new File(args[1]);
		rp.readConfigFile(f);


		//there will be 2 messages for each csenter request, so initializing with 2*total resource messages
		totalRequest = (2*rp.totalNode*rp.numberOfRequest);
		
		ServerSocket serverSock;
		try {
			serverSock = new ServerSocket(Integer.parseInt(args[0]));
			
			int totalRequest = (rp.totalNode*rp.numberOfRequest);
			//int totalRequest = 12;

			System.out.println("Resource Server started at port: "+args[0]);

			while(true)
			{
				Socket sock = serverSock.accept();
				
				CSrequestNodeListenerInstance cSrequestNodeListenerInstance = new CSrequestNodeListenerInstance(sock, rp);
				cSrequestNodeListenerInstance.start();
			
				//either cs enter or cs exit
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
	
	public synchronized void readCSMessage(Message m){
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
		}
	}
}
