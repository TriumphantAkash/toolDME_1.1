import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Main {

	Node node;
	int totalNode;
	int interRequestDelay;
	int csExecutionTime;
	int numberOfRequest;
	public static String resourceHostName;
	public static int resourcePortNumber;
	public static volatile boolean csEnter = false;
	Node resource;
	public static volatile boolean ackFromResourceForCSEnter = false;
	public static volatile boolean ackFromResourceForCSExit = false;
	public static HashMap<Integer, Client> clientThread;
	static Socket resourceSocket;
	static ObjectOutputStream resourceOOS;
	static Main m;
	public Main()
	{
		node = new Node();
		resource = new Node();
		this.clientThread = new HashMap<Integer, Client>();
		
		
	}

	public static void main(String[] args) {
		int nodeNumber = Integer.parseInt(args[0]);
		File f = new File(args[1]);

		m = new Main();
		m.node.setId(nodeNumber);
		m.readConfigFile(nodeNumber,f);
		m.resource.setHostname(args[2]);
		m.resource.setPortNumber(Integer.parseInt(args[3]));

		SocketConnectionServer server = new SocketConnectionServer(m);
		server.start();

		//create socket to the resources and use it later to send cs lock and cs unlock messages
		try {
			resourceSocket = new Socket(resourceHostName, resourcePortNumber);
			resourceOOS = new ObjectOutputStream(resourceSocket.getOutputStream());
		} catch (IOException e1) {

			System.out.println("Exception while creating socket for Resource");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
						
		try {
			Thread.sleep(3000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		for(Node n : m.node.getQuorum())
		{

			Client c = new Client(m.node,n,m);

			c.start();
			clientThread.put(n.getId(), c);
		}


		while(m.numberOfRequest>0)
			//int counter = 2;
			//while(counter>0)
		{
			if(m.node.getId()!=3)
			{

				m.csEnter();
				m.csExecution();
				m.csExit();
			}
			m.numberOfRequest = m.numberOfRequest - 1;
			//counter--;
			double lambda = 1.0 / m.interRequestDelay; 
			Random defaultR = new Random();
			try {        	
				long l = (long) m.getRandom(defaultR, lambda);
				Thread.sleep(l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}

	}

	public void readConfigFile(int nodeNumber, File f)
	{
		FileReader fileReader;
		try {
			fileReader = new FileReader(f);
			BufferedReader br = new BufferedReader(fileReader);
			ArrayList<Node> aln = new ArrayList<Node>();
			HashMap<Integer,Node> hostNameHM = new HashMap<Integer, Node>();

			String line1 = br.readLine();
			String[] words = line1.split("\\s+");
			totalNode = Integer.parseInt(words[0]);
			interRequestDelay = Integer.parseInt(words[1]);
			csExecutionTime = Integer.parseInt(words[2]);
			numberOfRequest = Integer.parseInt(words[3]);

			for(int i=0;i<totalNode;i++)
			{
				String line2= br.readLine();

				String[] hostNameLine = line2.split("\\s+");
				Node n = new Node();
				n.setId(Integer.parseInt(hostNameLine[0]));
				n.setHostname(hostNameLine[1]);

				n.setPortNumber(Integer.parseInt(hostNameLine[2]));
				hostNameHM.put(n.getId(), n);
				//aln.add(n);
			}

			HashMap<Integer,ArrayList<Node>> hm = new HashMap<Integer,ArrayList<Node>>();

			for(int i=0;i<totalNode;i++)
			{
				ArrayList<Node> quorum = new ArrayList<Node>();
				String line2= br.readLine();

				String[] childLine = line2.split("\\s+");
				for(int j=0;j<childLine.length;j++)
				{
					Node n = new Node();
					n.setId(Integer.parseInt(childLine[j]));
					n.setHostname(hostNameHM.get(n.getId()).getHostname());
					n.setPortNumber(hostNameHM.get(n.getId()).getPortNumber());
					quorum.add(n);

				}

				hm.put(i, quorum);
			}

			node.setHostname(hostNameHM.get(nodeNumber).getHostname());
			node.setPortNumber(hostNameHM.get(nodeNumber).getPortNumber());
			node.setQuorum(hm.get(nodeNumber));
			node.setVectorClock(new int[totalNode]);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void csEnter()
	{
		System.out.println("cs enter "+ node.getId());
		for(Integer i : clientThread.keySet())
		{
			clientThread.get(i).sendMessage("request");
		}

		while(!Main.csEnter)
		{
		}
		System.out.println("Exit CS Enter function");

	}

	public void csExecution()
	{
		//sending execution request to the resource csgrads1
		//		ArrayList<Message> almResource = new ArrayList<Message>();
		//		Message mResource = new Message();
		//		mResource.setSourceNode(node);
		//		//we will get the destination hostname fro command line argument
		//		mResource.setDestinationNode(resource);
		//		mResource.setMessage("csenter");
		//		almResource.add(mResource);
		//		
		//		SocketConnectionClient sccResource = new SocketConnectionClient(almResource);
		//		sccResource.start();
		//1) send cs lock message to the Resource process
		Message msg = new Message();
		msg.setMessage("csenter");
		msg.setSourceNode(m.node);
		try {
			resourceOOS.writeObject(msg);
			resourceOOS.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("exception while writing to resource process socket");
			e1.printStackTrace();
		}
		
		System.out.println("Application main time stamp" + node.getRequestTimestamp());
		node.vectorClock[node.getId()] = node.vectorClock[node.getId()]+1;
		double lambda = 1.0 / csExecutionTime; 
		Random defaultR = new Random();
		try {        	
			long l = (long) getRandom(defaultR, lambda);
			Thread.sleep(l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("CSExecution "+ node.getId());
		Main.csEnter = false;
		//		while(!ackFromResourceForCSEnter)
		//		{
		//			
		//		}
		//		ackFromResourceForCSEnter = false;

	}

	public void csExit()
	{
		//sending execution request to the resource csgrads1
		//				ArrayList<Message> almResource = new ArrayList<Message>();
		//				Message mResource = new Message();
		//				mResource.setSourceNode(node);
		//				//we will get the destination hostname fro command line argument
		//				mResource.setDestinationNode(resource);
		//				mResource.setMessage("csexit");
		//				almResource.add(mResource);
		//				
		//				SocketConnectionClient sccResource = new SocketConnectionClient(almResource);
		//				sccResource.start();

		Message msg = new Message();
		msg.setMessage("csexit");
		msg.setSourceNode(m.node);
		
		try {
			resourceOOS.writeObject(msg);
			resourceOOS.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("exception while writing 'csexit' to resource process socket");
			e1.printStackTrace();
		}
		
		for(Integer i : clientThread.keySet())
		{
			clientThread.get(i).sendMessage("release");
		}
		//1) unlock the resource (send unlock message to Resource Process)
		
		
		//		while(!ackFromResourceForCSExit)
		//		{
		//			
		//		}
		//		ackFromResourceForCSExit = false;


	}

	public double getRandom(Random r, double p) { 
		double d = -(Math.log(r.nextDouble()) / p);
		return d;
	}


}
