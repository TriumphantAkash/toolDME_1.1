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

	static Socket resourceSocket;
	static ObjectOutputStream resourceOOS;
	static Main m;

	ArrayList<Node> queue;
	MinHeap mn;

	public Main()
	{
		node = new Node();
		resource = new Node();
		this.clientThread = new HashMap<Integer, Client>();

		this.mn = new MinHeap();
		queue = new ArrayList<Node>();
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

			Client c = new Client(n,m);

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
		Message request = new Message();
		request.setMessage("request");
		request.setSourceNode(node);
		System.out.println("Request time stamp : "+request.getSourceNode().getRequestTimestamp());
		System.out.println("cs enter "+ node.getId());
		for(Integer i : clientThread.keySet())
		{
			//clientThread.get(i).sendMessage("request");
			System.out.println(request.getMessage() + "-"+request.getSourceNode().getId() + i);
			clientThread.get(i).sendMessage(request);
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

		Message release = new Message();
		release.setMessage("release");
		release.setSourceNode(node);
		
		for(Integer i : clientThread.keySet())
		{
			//clientThread.get(i).sendMessage("release");
			System.out.println(release.getMessage() + "-"+release.getSourceNode().getId() + i);
			clientThread.get(i).sendMessage(release);
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
	
	public synchronized void grant(Message m)
	{
		System.out.println("Message "+ m.getMessage() + " Source "+ m.getSourceNode().getId() + " Destination "+m.getDestinationNode().getId());
		if(node.getTimestamp() < m.getSourceNode().getTimestamp()){
			node.setTimestamp(m.getSourceNode().getTimestamp()+1);
			System.out.println("Time stamp if " + node.getTimestamp());
		}else {
			node.setTimestamp(node.getTimestamp()+1);
			System.out.println("Time stamp else " + node.getTimestamp());
			
		}
//		node.getGrant().add(m.getSourceNode());
		node.grant.put(m.getSourceNode().getId(), m.getSourceNode());
		System.out.print("Node "+ node.getId() + " Grant list ");
		for(Integer n: node.grant.keySet())
		{
			System.out.print("(" + node.grant.get(n).getId() + "),");
		}
		System.out.println();
		
	
		//node.setFailedList(removeElementFromList(node.getFailedList(), m.getSourceNode().getId()));
		node.deleteFromFailedList(m.getSourceNode().getId());

		//2) check size of grantArrayList 
		if(node.grant.size() == node.getQuorum().size())
		{
			synchronized(this)
			{
				
				Main.csEnter = true;
				node.grant.clear();
				node.setRequestTimestamp(node.getTimestamp());
				//System.out.println("Main request timestamp" + node.getRequestTimestamp());
			}
			//go into critical section
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
		if(node.getFailedList().size()>0)
		{	
			
			node.deleteFromGrant(m.getSourceNode().getId());
			node.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
			//node.getInquireQuorum().add(m.getSourceNode());
			System.out.print("Inquire List ");
			for(Integer n: node.inquireQuorum.keySet())
			{
				System.out.print("(" + node.inquireQuorum.get(n).getId() + ")");
			}
			System.out.println();
			
			node.setTimestamp(node.getTimestamp()+1);
			
			for(Integer n:node.inquireQuorum.keySet())
			{
				Message m1 = new Message();
				m1.setMessage("yield");
				m1.setSourceNode(node);
				//Main.clientThread.get(n).sendMessage("yield");
				Main.clientThread.get(n).sendMessage(m1);
				/*Message m1 = new Message();
				m1.setDestinationNode(node.inquireQuorum.get(n));
				m1.setSourceNode(node);
				m1.setMessage("yield");
				sendMessage(m1);*/
			}
			
			node.inquireQuorum.clear();

		}
		else
		{
			node.inquireQuorum.put(m.getSourceNode().getId(), m.getSourceNode());
			System.out.println("else inquire");
			System.out.print("Node "+ node.getId() +"Inquire List ");
			for(Integer n:node.inquireQuorum.keySet())
			{
				System.out.print("(" + node.inquireQuorum.get(n).getId() + ")");
			}
			System.out.println();
		}
	}
	public synchronized Message release(Message msg)
	{
		System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
		if(node.getTimestamp() < msg.getSourceNode().getTimestamp()){
			node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		//1) delete first element from the main queue
		//node.getQueue().remove(0);
		queue.remove(0);
		System.out.println("HAHAHAHAHAHA");
		//for(Node n : node.getQueue())
		for(Node n : queue)
		{
			
			System.out.println(n.getId() + " " + n.getRequestTimestamp() + " " + n.getTimestamp());
		}
		//mn.minHeapify(node.getQueue(), 0);
		node.setGrantFlag(false);
		
		//2) Add waitingForYield list to original queue
		for(Entry<Integer, Node> n : node.getWaitingForYield().entrySet())
		{
			//node.getQueue().add(n.getValue());
			queue.add(n.getValue());
		}
		
		node.setWaitingForYield(new HashMap<Integer, Node>());
		//if(node.getQueue().size()>0)
		if(queue.size()>0)
		{
			System.out.println("Before build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			//for(Node n : node.getQueue())
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
			}
			System.out.println();
			
			//mn.buildMinHeap(node.getQueue());
			mn.buildMinHeap(queue);
			
			System.out.println("After build heap");
			System.out.print("Node "+node.getId() + " PQ ");
			//for(Node n : node.getQueue())
			for(Node n : queue)
			{
				System.out.print("("+n.getRequestTimestamp()+","+n.getId()+",");
			}
			System.out.println();
			
			//node.setGrantOwner(node.getQueue().get(0));
			node.setGrantOwner(queue.get(0));
			Message sendGrant = new Message();
			sendGrant.setMessage("grant");
			sendGrant.setSourceNode(node);
			//sendGrant.setDestinationNode(node.getQueue().get(0));
			sendGrant.setDestinationNode(queue.get(0));
			node.setGrantFlag(true);
			return sendGrant;
			/*try {
				//writeMessage(sendGrant);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

		}
		return null;
	}
	public synchronized Message request(Message msg)
	{
		System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
		if(node.getTimestamp() < msg.getSourceNode().getTimestamp()){
			node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		if(!node.isGrantFlag())
		{	//if this is first request, grant Flag is false
			//node.getQueue().add(msg.getSourceNode());
			queue.add(msg.getSourceNode());
			System.out.println("Node "+ node.getId() + " PQ first"+ queue.get(0).getId() + " RTS "+ queue.get(0).getRequestTimestamp() + " TS " +queue.get(0).getTimestamp());
//			System.out.println("printing source");
//			System.out.println("Node "+ node.getId() + " PQ first"+ msg.getSourceNode().getId() + " RTS "+ msg.getSourceNode().getRequestTimestamp() + " TS " +msg.getSourceNode().getTimestamp());
			
			//send grant to the source of msg
			node.setTimestamp(node.getTimestamp()+1);
			Message grantMsg = new Message();
			grantMsg.setSourceNode(node);
			grantMsg.setDestinationNode(msg.getSourceNode());
			grantMsg.setMessage("grant");
			node.setGrantFlag(true);
			node.setGrantOwner(msg.getSourceNode());	
			return grantMsg;
			/*try {
				writeMessage(grantMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
									
		}
		else 
		{
			//if(msg.getSourceNode().getTimestamp() > node.getGrantOwner().getTimestamp())
			//if(node.getQueue().size()>0)
			if(queue.size()>0)
			{
				System.out.println("Else");
				System.out.println("Node "+ node.getId() + " PQ first "+ queue.get(0).getId() + " TS "+ queue.get(0).getRequestTimestamp());
				System.out.println("Node "+ node.getId() + " Src "+ msg.getSourceNode().getId() + " TS "+ msg.getSourceNode().getRequestTimestamp());
				//if((msg.getSourceNode().getRequestTimestamp() > node.getQueue().get(0).getRequestTimestamp()) || ((msg.getSourceNode().getRequestTimestamp()==node.getQueue().get(0).getRequestTimestamp()) && msg.getSourceNode().getId()>node.getQueue().get(0).getId()))
				if((msg.getSourceNode().getRequestTimestamp() > queue.get(0).getRequestTimestamp()) || ((msg.getSourceNode().getRequestTimestamp()==queue.get(0).getRequestTimestamp()) && msg.getSourceNode().getId()>queue.get(0).getId()))
				{	//msg's timestamp is more than grant owner's timestamp
					//put this req msg into the original priority queue
			
					//node.getQueue().add(msg.getSourceNode());
					queue.add(msg.getSourceNode());
					
					System.out.println("Before build heap");
					System.out.print("Node "+node.getId() + " PQ ");
					//for(Node n : node.getQueue())
					for(Node n : queue)
					{
						System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
					}
					System.out.println();
					//mn.buildMinHeap(node.getQueue());
					mn.buildMinHeap(queue);
					
					System.out.println("After build heap");
					System.out.print("Node "+node.getId() + " PQ ");
					//for(Node n : node.getQueue())
					for(Node n : queue)
					{
						System.out.print("("+n.getRequestTimestamp()+","+n.getId()+"),");
					}
					System.out.println();
					//ArrayList<Message> alm = new ArrayList<Message>();
					Message sendFailed = new Message();
					sendFailed.setSourceNode(node);
					sendFailed.setDestinationNode(msg.getSourceNode());
					sendFailed.setMessage("failed");
					return sendFailed;
					/*try {
						writeMessage(sendFailed);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/	
				}else
				{
					node.getWaitingForYield().put(msg.getSourceNode().getId(), msg.getSourceNode());	//add this req to waitingForYield list
					if(!node.isInquireFlag()) 
					{//check inquire Flag so that don't send inquire to a node again and again
						//send inquire to grant owner
						node.setTimestamp(node.getTimestamp()+1);
						Message inquireMsg = new Message();
						inquireMsg.setSourceNode(node);
						inquireMsg.setDestinationNode(node.getGrantOwner());
						inquireMsg.setMessage("inquire");
						node.setInquireFlag(true);
						return inquireMsg;
						/*try {
							writeMessage(inquireMsg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/		
					} 
				}
			}
			
		}
		return null;
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
		node.failedList.put(m.getSourceNode().getId(), m.getSourceNode());
		System.out.println("Node "+node.getId() + " inside failed : inqQuorum size "+ node.getInquireQuorum().size());
		if(node.getInquireQuorum().size()>0)
		{
			
			//for(Node n: node.getInquireQuorum())
			for(Integer n : node.inquireQuorum.keySet())
			{
				System.out.println("Node "+node.getId() + " inside failed inq quorum : "+ node.inquireQuorum.get(n).getId());
				Message send = new Message();
				send.setMessage("yield");
				send.setSourceNode(node);
				Main.clientThread.get(n).sendMessage(send);
				//Main.clientThread.get(n).sendMessage("yield");
				
				/*Message send = new Message();
				node.setTimestamp(node.getTimestamp()+1);
				send.setDestinationNode(destinationNode);
				send.setSourceNode(sourceNode);
				send.setMessage("yield");
				sendMessage(send);*/
				
				node.deleteFromGrant(n);
				//node.setGrant(removeElementFromList(node.getGrant(), n.getId()));

										
				//alm.add(send);
				System.out.print("Node " + node.getId() + " Grant list after deletion ");
				//for(Node a: node.getGrant())
				for(Integer a: node.grant.keySet())
				{
					System.out.print(node.grant.get(a).getId() + ",");
				}
				System.out.println();
				
			}
			System.out.println("Check alm Node "+ node.getId());
			node.inquireQuorum.clear();
			//node.setInquireQuorum(new ArrayList<Node>());
//			SocketConnectionClient c = new SocketConnectionClient(alm);
//			c.start();	
		}
	}
	public synchronized Message yield(Message msg)
	{
		System.out.println("Message "+ msg.getMessage() + " Source "+ msg.getSourceNode().getId() + " Destination "+msg.getDestinationNode().getId());
		if(node.getTimestamp() < msg.getSourceNode().getTimestamp()){
			node.setTimestamp(msg.getSourceNode().getTimestamp()+1);
		}else {
			node.setTimestamp(node.getTimestamp()+1);
		}
		if(node.getWaitingForYield().size()>0)
		{
			for(Entry<Integer, Node> n : node.getWaitingForYield().entrySet())
			{
				//node.getQueue().add(n.getValue());
				queue.add(n.getValue());
			}
			
			//mn.buildMinHeap(node.getQueue());
			mn.buildMinHeap(queue);
			node.setTimestamp(node.getTimestamp()+1);
			Message send = new Message();
			send.setSourceNode(node);
			//send.setDestinationNode(node.getQueue().get(0));
			send.setDestinationNode(queue.get(0));
			send.setMessage("grant");
			System.out.println("Node "+node.getId()+" inside yield first of pq "+queue.get(0).getId());
			node.setWaitingForYield(new HashMap<Integer, Node>());
			return send;
			/*try {
				writeMessage(send);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

		}
		return null;
	}
	

}
