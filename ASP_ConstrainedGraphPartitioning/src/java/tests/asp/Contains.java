package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;

@DefAtom("contains")
public class Contains {

	private Partition partition;
	private Node node;

	public Contains() {
	}

	public Contains(Partition partition,Node node) {
		this.node = node;
		this.partition = partition;
	}

	@Arg(0)
	public Partition getPartition() {
		return partition;
	}

	public void setPartition(Partition partition) {
		this.partition = partition;
	}

	@Arg(1)
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
		final Contains other = (Contains) obj;
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
		return "Contains{" + "Partition=" + partition + ", contains=" + node + '}';
	}

}