package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;

@DefAtom("belongs")
public class Belongs {

	private Partition partition;
	private Node node;

	public Belongs() {
	}

	public Belongs(Node node, Partition partition) {
		this.node = node;
		this.partition = partition;
	}

	@Arg(1)
	public Partition getPartition() {
		return partition;
	}

	public void setPartition(Partition partition) {
		this.partition = partition;
	}

	
	@Arg(0)
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.partition);
		hash = 23 * hash + Objects.hashCode(this.node);
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
		final Belongs other = (Belongs) obj;
		if (!Objects.equals(this.partition, other.partition)) {
			return false;
		}
		if (!Objects.equals(this.node, other.node)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Belongs{" + "Node=" + node + ", to=" + partition + '}';
	}

}