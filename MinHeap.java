import java.util.ArrayList;


public class MinHeap {

	int heapSize;

	public MinHeap()
	{
		heapSize =0;
	}

	public void buildMinHeap(ArrayList<Node> al)
	{
		heapSize = al.size();
		for(int i=(al.size()/2) -1;i>=0;i--)
		{
			minHeapify(al, i);
		}
	}

	public void minHeapify(ArrayList<Node> al, int index)
	{
		int left = (2*index)+1;
		int right = (2*index)+2;
		heapSize = al.size();
		int smallest = index;

		

		if(left<heapSize && al.get(left).getRequestTimestamp()<=al.get(index).getRequestTimestamp())
		{
			if(al.get(left).getRequestTimestamp()<al.get(index).getRequestTimestamp())

				smallest = left;
			else if(al.get(left).getId()<al.get(index).getId())
			{
				smallest = left;
			}
		}

		if(right<heapSize && al.get(right).getRequestTimestamp()<=al.get(smallest).getRequestTimestamp())
		{
			if(al.get(right).getRequestTimestamp()<al.get(smallest).getRequestTimestamp())

				smallest = right;
			else if((al.get(right).getId()<al.get(smallest).getId()))
				smallest = right;
		}
		if(smallest!=index)
		{
			Node temp = al.get(index);
			al.set(index, al.get(smallest));
			al.set(smallest, temp);

			minHeapify(al, smallest);
		}

	}

	public void delete(ArrayList<Node>al)
	{
		int index = al.size()-1;
		Node temp = al.get(index);
		al.set(index, al.get(0));
		al.set(0, temp);
		heapSize = al.size() -1;
		al.remove(al.size()-1);
		minHeapify(al, 0);
	}


}
