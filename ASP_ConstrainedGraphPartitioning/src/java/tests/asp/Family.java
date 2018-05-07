package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;

@DefAtom("family")
public class Family {

	private Partition partition;
	private Node first;
	private Node second;
	public Family() {
	}

	public Family(Node first,Node second, Partition partition) {
		this.first = first;
		this.second = second;
		this.partition = partition;
	}
	
	@Arg(0)
	public Node getFirst() {
		return first;
	}

	public void setFirst(Node first) {
		this.first = first;
	}
	@Arg(1)
	public Node getSecond() {
		return second;
	}

	public void setSecond(Node second) {
		this.second = second;
	}

	@Arg(2)
	public Partition getPartition() {
		return partition;
	}

	public void setPartition(Partition partition) {
		this.partition = partition;
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.partition);
		hash = 23 * hash + Objects.hashCode(this.first);
		hash = 23 * hash + Objects.hashCode(this.second);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Family other = (Family) obj;
		if (!Objects.equals(this.partition, other.partition)) {
			return false;
		}
		if (!Objects.equals(this.first, other.first)) {
			return false;
		}
		if (!Objects.equals(this.second, other.second)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Family{" + "1st =" + first+ "2nd =" + second+ ", Partition=" + partition + '}';
	}

}