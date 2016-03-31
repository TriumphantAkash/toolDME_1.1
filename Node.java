import java.io.Serializable;
import java.util.ArrayList;


public class Node implements Serializable{

	private int id;
	private int portNumber;
	private String hostname;
	private ArrayList<Node> quorum;
	private ArrayList<Node> queue;
	private Node grantOwner;
	private boolean grantFlag;
	private boolean inquireFlag;
	private int timestamp =1;
	private ArrayList<Node> grant;
	private ArrayList<Node> waitingForYield;
	private ArrayList<Node> inquireQuorum;
	private ArrayList<Node> failedList;
	private int requestTimestamp =1;
	public int[] vectorClock;
	public Node()
	{
		grant = new ArrayList<Node>();
		waitingForYield = new ArrayList<Node>();
		inquireQuorum = new ArrayList<Node>();
		failedList = new ArrayList<Node>();
		queue = new ArrayList<Node>();
		quorum = new ArrayList<Node>();
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public ArrayList<Node> getQuorum() {
		return quorum;
	}
	public void setQuorum(ArrayList<Node> quorum) {
		this.quorum = quorum;
	}
	public ArrayList<Node> getQueue() {
		return queue;
	}
	public void setQueue(ArrayList<Node> queue) {
		this.queue = queue;
	}
	public Node getGrantOwner() {
		return grantOwner;
	}
	public void setGrantOwner(Node grantOwner) {
		this.grantOwner = grantOwner;
	}
	public boolean isGrantFlag() {
		return grantFlag;
	}
	public void setGrantFlag(boolean grantFlag) {
		this.grantFlag = grantFlag;
	}
	public boolean isInquireFlag() {
		return inquireFlag;
	}
	public void setInquireFlag(boolean inquireFlag) {
		this.inquireFlag = inquireFlag;
	}
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public ArrayList<Node> getGrant() {
		return grant;
	}
	public void setGrant(ArrayList<Node> grant) {
		this.grant = grant;
	}

	public ArrayList<Node> getWaitingForYield() {
		return waitingForYield;
	}
	public void setWaitingForYield(ArrayList<Node> waitingForYield) {
		this.waitingForYield = waitingForYield;
	}
	public ArrayList<Node> getInquireQuorum() {
		return inquireQuorum;
	}
	public void setInquireQuorum(ArrayList<Node> inquireQuorum) {
		this.inquireQuorum = inquireQuorum;
	}
	public ArrayList<Node> getFailedList() {
		return failedList;
	}
	public void setFailedList(ArrayList<Node> failedList) {
		this.failedList = failedList;
	}

	public int getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(int requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public int[] getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(int[] vectorClock) {
		this.vectorClock = vectorClock;
	}
	
	
	
	
}
