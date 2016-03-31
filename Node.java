import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


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
	public HashMap<Integer,Node> grant;
	public HashMap<Integer,Node> waitingForYield;
	public HashMap<Integer,Node> inquireQuorum;
	public HashMap<Integer,Node> failedList;
	private int requestTimestamp =1;
	public int[] vectorClock;
	public Node()
	{
		grant = new HashMap<Integer,Node>();
		waitingForYield = new HashMap<Integer,Node>();
		inquireQuorum = new HashMap<Integer,Node>();
		failedList = new HashMap<Integer,Node>();
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
