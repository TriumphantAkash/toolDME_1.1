import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;


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
	public static HashMap<Integer,Integer> requestTimeStamp;
	public static String serverName;
	public static Integer serverPortNumber;
	public static Integer serverNodeId;
	public static Node serverNode;
	static Socket resourceSocket;
	static ObjectOutputStream resourceOOS;
	static ObjectInputStream resourceOIS;
	static Main main;
	static boolean resourceFlag = true;
	public static HashMap<Integer,Node> hostNameHM;
	public static HashMap<Integer,Node> sendFailed;
	ArrayList<Node> queue;
	MinHeap mn;
	public HashMap<Integer,Node> grant;
	public HashMap<Integer,Node> waitingForYield;
	public HashMap<Integer,Node> inquireQuorum;
	public HashMap<Integer,Node> failedList;

	public Main()
	{
		node = new Node();
		resource = new Node();
		this.clientThread = new HashMap<Integer, Client>();
		requestTimeStamp = new HashMap<Integer, Integer>();
		this.mn = new MinHeap();
		queue = new ArrayList<Node>();
		serverNode = new Node();
		sendFailed = new HashMap<Integer, Node>();
		
		grant = new HashMap<Integer,Node>();
		waitingForYield = new HashMap<Integer,Node>();
		inquireQuorum = new HashMap<Integer,Node>();
		failedList = new HashMap<Integer,Node>();
	}

	public static void main(String[] args) {
		int nodeNumber = Integer.parseInt(args[0]);
		File f = new File(args[1]);

		main = new Main();
		main.node.setId(nodeNumber);
		main.readConfigFile(nodeNumber,f);
		main.resource.setHostname(args[2]);
		main.resource.setPortNumber(Integer.parseInt(args[3]));
		Main.serverNode.setId(Main.serverNodeId);
		Main.serverNode.setHostname(Main.serverName);
		Main.serverNode.setPortNumber(Main.serverPortNumber);


		if(serverNodeId==nodeNumber)
		{
			SocketConnectionServer server = new SocketConnectionServer(serverNode,main);
			server.start();
		}
		//create socket to the resources and use it later to send cs lock and cs unlock messages

		try {
			Thread.sleep(3000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*for(Node n : m.node.getQuorum())
		{

			Client c = new Client(n,m);

			c.start();
			clientThread.put(n.getId(), c);
		}*/
		Client c = new Client(Main.serverNode,main);
		c.start();
		clientThread.put(Main.serverNodeId, c);
		Message temp = new Message();
		temp.setMessage("hello");
		temp.setSourceNode(main.node);
		clientThread.get(Main.serverNodeId).sendMessage(temp);
		try {
			resourceSocket = new Socket(main.resource.getHostname(), main.resource.getPortNumber());
			resourceOOS = new ObjectOutputStream(resourceSocket.getOutputStream());

		} catch (IOException e1) {

			System.out.println("Exception while creating socket for Resource");
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

		while(main.numberOfRequest>0)
			//int counter = 2;
			//while(counter>0)
		{

			//if(m.node.getId()!=0)
			//{
			main.csEnter();
			main.csExecution();
			main.csExit();
			//}
			main.numberOfRequest = main.numberOfRequest - 1;
			//counter--;
			double lambda = 1.0 / main.interRequestDelay; 
			Random defaultR = new Random();
			try {        	
				long l = (long) main.getRandom(defaultR, lambda);
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
			hostNameHM = new HashMap<Integer, Node>();

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
				if(i==0)
				{
					serverNodeId = Integer.parseInt(hostNameLine[0]);
					serverName = hostNameLine[1];
					serverPortNumber = Integer.parseInt(hostNameLine[2]);
				}
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
		for(Node n: node.getQuorum())
		{
			Message request = new Message();
			request.setMessage("request");
			request.setSourceNode(node);
			request.setDestinationNode(n);
			System.out.println("Request time stamp : "+request.getSourceNode().getRequestTimestamp());
			System.out.println("cs enter "+ node.getId());

			/*for(Integer i : clientThread.keySet())
		{
			//clientThread.get(i).sendMessage("request");

			clientThread.get(i).sendMessage(request);
		}*/

			clientThread.get(serverNodeId).sendMessage(request);
		}
		while(!Main.csEnter)
		{
		}
		System.out.println("Exit CS Enter function");

	}

	public void csExecution()
	{

		Message msg = new Message();
		msg.setMessage("csenter");
		msg.setSourceNode(main.node);
		try {
			resourceOOS.writeObject(msg);
			resourceOOS.flush();
			/*resourceOOS.writeUnshared(msg);
			resourceOOS.flush();*/
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("exception while writing to resource process socket");
			e1.printStackTrace();
		}

		System.out.println("Application main time stamp" + node.getRequestTimestamp());

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
		try {
			if(resourceFlag)
			{
				resourceOIS = new ObjectInputStream(resourceSocket.getInputStream());
				Message r= (Message)resourceOIS.readObject();
				resourceFlag = false;
			}
			else
			{
				Message d= (Message)resourceOIS.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void csExit()
	{
		Message msg = new Message();
		msg.setMessage("csexit");
		msg.setSourceNode(main.node);

		try {
			resourceOOS.writeObject(msg);
			resourceOOS.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("exception while writing 'csexit' to resource process socket");
			e1.printStackTrace();
		}
		for(Node n : node.getQuorum())
		{
			main.inquireQuorum.clear();
			main.failedList.clear();
			Message release = new Message();
			release.setMessage("release");
			release.setSourceNode(node);
			release.setDestinationNode(n);
			clientThread.get(serverNodeId).sendMessage(release);
			main.inquireQuorum.clear();
			main.failedList.clear();
		}
		node.setRequestTimestamp(node.getTimestamp()+1);
		}

	public double getRandom(Random r, double p) { 
		double d = -(Math.log(r.nextDouble()) / p);
		return d;
	}
	public synchronized void request(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		System.out.println("Node "+ node.getId() + " PQ size"+ queue.size());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		if(!node.isGrantFlag())
		{	//if this is first request, grant Flag is false
			queue.add(m.getSourceNode());
			System.out.println("Node "+ node.getId() + " PQ first"+ queue.get(0).getId() + " TS "+ queue.get(0).getRequestTimestamp());
			
			//send grant to the source of m
			node.setTimestamp(node.getTimestamp()+1);
			
			Message grantMsg = new Message();
			grantMsg.setSourceNode(node);
			grantMsg.setDestinationNode(m.getSourceNode());
			grantMsg.setMessage("grant");
			node.setGrantFlag(true);
			node.setGrantOwner(m.getSourceNode());
			clientThread.get(serverNodeId).sendMessage(grantMsg);
			
		}
		else 
		{
			//if(m.getSourceNode().getTimestamp() > node.getGrantOwner().getTimestamp())
			if(queue.size()>0)
			{
				System.out.println("Else");
				System.out.println("Node "+ node.getId() + " PQ first "+ queue.get(0).getId() + " TS "+ queue.get(0).getRequestTimestamp());
				System.out.println("Node "+ node.getId() + " Src "+ m.getSourceNode().getId() + " TS "+ m.getSourceNode().getRequestTimestamp());
				if((m.getSourceNode().getRequestTimestamp() > queue.get(0).getRequestTimestamp()) || ((m.getSourceNode().getRequestTimestamp()==queue.get(0).getRequestTimestamp()) && m.getSourceNode().getId()>queue.get(0).getId()))
				{	//m's timestamp is more than grant owner's timestamp
					//put this req m into the original priority queue
			
					queue.add(m.getSourceNode());
					
					System.out.println("Before build heap");
					System.out.print("Node "+node.getId() + " PQ ");
					for(Node n : queue)
					{
						System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
					}
					System.out.println();
					mn.buildMinHeap(queue);
					
					System.out.println("After build heap");
					System.out.print("Node "+node.getId() + " PQ ");
					for(Node n : queue)
					{
						System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
					}
					System.out.println();
					
					Message sendFailed = new Message();
					sendFailed.setSourceNode(node);
					sendFailed.setDestinationNode(m.getSourceNode());
					sendFailed.setMessage("failed");
					clientThread.get(serverNodeId).sendMessage(sendFailed);	
				}else
				{
					//node.getWaitingForYield().add(m.getSourceNode());	//add this req to waitingForYield list
					main.waitingForYield.put(m.getSourceNode().getId(), m.getSourceNode());
					System.out.print("Node "+node.getId() + " wait list");
					for(Integer i: main.waitingForYield.keySet())
					{
						System.out.print("("+i+")");
					}
					System.out.println();
					if(!node.isInquireFlag()) 
					{//check inquire Flag so that don't send inquire to a node again and again
						//send inquire to grant owner
						node.setTimestamp(node.getTimestamp()+1);
						
						Message inquireMsg = new Message();
						inquireMsg.setSourceNode(node);
						inquireMsg.setDestinationNode(node.getGrantOwner());
						inquireMsg.setMessage("inquire");
						node.setInquireFlag(true);
						clientThread.get(serverNodeId).sendMessage(inquireMsg);		
					} 
				}
			}
			
		}

		
	}
	public synchronized void release(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		//1) delete first element from the main queue
		queue.remove(0);
		//mn.minHeapify(queue, 0);
		node.setGrantFlag(false);
		node.setInquireFlag(false);
		//2) Add waitingForYield list to original queue
		/*for(Node n:node.getWaitingForYield())
		{
			queue.add(n);
		}*/
		sendFailed.putAll(main.waitingForYield);
		for(Integer i : main.waitingForYield.keySet())
		{
			queue.add(main.waitingForYield.get(i));
			
		}
		
		//node.setWaitingForYield(new ArrayList<Node>());
		main.waitingForYield.clear();
		if(queue.size()>0)
		{
			System.out.println("Before build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
			}
			System.out.println();
			
			mn.buildMinHeap(queue);
			
			System.out.println("After build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+",");
			}
			System.out.println();
			
			sendFailed.remove(queue.get(0).getId());
			for(Integer i : sendFailed.keySet())
			{
				Message failed = new Message();
				failed.setMessage("failed");
				failed.setSourceNode(node);
				failed.setDestinationNode(sendFailed.get(i));
				clientThread.get(serverNodeId).sendMessage(failed);
			}
			
			sendFailed.clear();
			node.setGrantOwner(queue.get(0));
			
			Message sendGrant = new Message();
			sendGrant.setMessage("grant");
			sendGrant.setSourceNode(node);
			sendGrant.setDestinationNode(queue.get(0));
			node.setGrantFlag(true);
			clientThread.get(serverNodeId).sendMessage(sendGrant);

		}
		
	}

	public synchronized void grant(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		//node.getGrant().add(m.getSourceNode());
		main.grant.put(m.getSourceNode().getId(), m.getSourceNode());
		System.out.print("Node "+ node.getId() + " Grant list ");
		//for(Node n: node.getGrant())
		for(Integer i : main.grant.keySet())
		{
			System.out.print("(" + i + "),");
		}
		System.out.println();
		
	
		//node.setFailedList(removeElementFromList(node.getFailedList(), m.getSourceNode().getId()));
		main.failedList.remove(m.getSourceNode().getId());

		//2) check size of grantArrayList 
		if(main.grant.size() == node.getQuorum().size())
		{
			synchronized(this)
			{
				
				Main.csEnter = true;
				//node.setGrant(new ArrayList<Node>());
				main.grant.clear();
				main.inquireQuorum.clear();
				node.setRequestTimestamp(node.getTimestamp());
			}
			//go into critical section
		}

	}
	public synchronized void yield(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		node.setInquireFlag(false);
		if(main.getWaitingForYield().size()>0)
		{
			//for(Node n : node.getWaitingForYield())
			sendFailed.putAll(main.waitingForYield);
			for(Integer n : main.waitingForYield.keySet())
			{
				queue.add(main.waitingForYield.get(n));
			}
			System.out.println("Before build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
			}
			System.out.println();
			mn.buildMinHeap(queue);
			
			System.out.println("After build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
			}
			System.out.println();
			
			//mn.buildMinHeap(queue);
			sendFailed.remove(queue.get(0).getId());
			for(Integer i : sendFailed.keySet())
			{
				Message failed = new Message();
				failed.setMessage("failed");
				failed.setSourceNode(node);
				failed.setDestinationNode(sendFailed.get(i));
				clientThread.get(serverNodeId).sendMessage(failed);
			}
			
			sendFailed.clear();
			
			
			node.setTimestamp(node.getTimestamp()+1);
			main.waitingForYield.clear();
			Message send = new Message();
			send.setSourceNode(node);
			send.setDestinationNode(queue.get(0));
			send.setMessage("grant");
			System.out.println("Node "+node.getId()+" inside yield first of pq "+queue.get(0).getId());
			//node.setWaitingForYield(new ArrayList<Node>());
			
			clientThread.get(serverNodeId).sendMessage(send);	

		}

		
	}

	public synchronized void inquire(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		if(main.getFailedList().size()>0)
		{	
			System.out.println("Nilesh");
			
			
			//node.setGrant(removeElementFromList(node.getGrant(), m.getSourceNode().getId()));
			main.grant.remove(m.getSourceNode().getId());

			//node.getInquireQuorum().add(m.getSourceNode());
			main.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
			System.out.print("Inquire List ");
			//for(Node n: node.getInquireQuorum())
			for(Integer n : main.inquireQuorum.keySet())
			{
				System.out.print("(" + n + ")");
			}
			System.out.println();
			
			node.setTimestamp(node.getTimestamp()+1);
			
			for(Integer n : main.inquireQuorum.keySet())
			{
				main.failedList.put(n, main.inquireQuorum.get(n));
				Message m1 = new Message();
				m1.setDestinationNode(main.inquireQuorum.get(n));
				m1.setSourceNode(node);
				m1.setMessage("yield");
				clientThread.get(serverNodeId).sendMessage(m1);
			}
			
			//node.setInquireQuorum(new ArrayList<Node>());
			main.inquireQuorum.clear();
		}
		else
		{
			//node.getInquireQuorum().add(m.getSourceNode());
			main.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
			System.out.println("else inquire");
			System.out.print("Node "+ node.getId() +"Inquire List ");
			//for(Node n: node.getInquireQuorum())
			for(Integer n : main.inquireQuorum.keySet())
			{
				System.out.print("(" + n + ")");
			}
			System.out.println();
		}
	}
	
	
	public synchronized void failed(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		//node.getFailedList().add(m.getSourceNode());
		main.failedList.put(m.getSourceNode().getId(), m.getSourceNode());
		System.out.println("Node "+node.getId() + " inside failed : inqQuorum size "+ main.getInquireQuorum().size());
		if(main.getInquireQuorum().size()>0)
		{
			
			//for(Node n: node.getInquireQuorum())
			for(Integer n : main.inquireQuorum.keySet())
			{
				System.out.println("Node "+node.getId() + " inside failed inq quorum : "+ n);
				main.failedList.put(n, main.inquireQuorum.get(n));
				Message send = new Message();
				node.setTimestamp(node.getTimestamp()+1);
				send.setDestinationNode(main.inquireQuorum.get(n));
				send.setSourceNode(node);
				send.setMessage("yield");
				clientThread.get(serverNodeId).sendMessage(send);
				
				//node.setGrant(removeElementFromList(node.getGrant(), n.getId()));
				main.grant.remove(n);

										
				
				System.out.print("Node " + node.getId() + " Grant list after deletion ");
				for(Integer a: main.grant.keySet())
				{
					System.out.print(a + ",");
				}
				System.out.println();
				
			}
			
			//node.setInquireQuorum(new ArrayList<Node>());
			main.inquireQuorum.clear();
			
		}

	}
	
	public HashMap<Integer, Node> getGrant() {
		return grant;
	}

	public void setGrant(HashMap<Integer, Node> grant) {
		this.grant = grant;
	}

	public HashMap<Integer, Node> getWaitingForYield() {
		return waitingForYield;
	}

	public void setWaitingForYield(HashMap<Integer, Node> waitingForYield) {
		this.waitingForYield = waitingForYield;
	}

	public HashMap<Integer, Node> getInquireQuorum() {
		return inquireQuorum;
	}

	public void setInquireQuorum(HashMap<Integer, Node> inquireQuorum) {
		this.inquireQuorum = inquireQuorum;
	}

	public HashMap<Integer, Node> getFailedList() {
		return failedList;
	}

	public void setFailedList(HashMap<Integer, Node> failedList) {
		this.failedList = failedList;
	}
	
	public synchronized void deleteFromGrant(int id)
	{
		grant.remove(id);
	}
	public synchronized void deleteFromWaitingForYield(int id)
	{
		waitingForYield.remove(id);
	}
	public synchronized void deleteFromInquireQuorum(int id)
	{
		inquireQuorum.remove(id);
	}
	public synchronized void deleteFromFailedList(int id)
	{
		failedList.remove(id);
	}
}
	

