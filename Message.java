import java.io.Serializable;


public class Message implements Serializable{
	private String message;
	private Node sourceNode;
	private Node destinationNode;
	public int[] vectorClock;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Node getSourceNode() {
		return sourceNode;
	}
	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}
	public Node getDestinationNode() {
		return destinationNode;
	}
	public void setDestinationNode(Node destinationNode) {
		this.destinationNode = destinationNode;
	}
	public int[] getVectorClock() {
		return vectorClock;
	}
	public void setVectorClock(int[] vectorClock) {
		this.vectorClock = vectorClock;
	}
	
}
