package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;

@DefAtom("edge_q")
public class QuotientEdge {

	private Partition from;
	private Partition to;

	public QuotientEdge() {
	}


	public QuotientEdge(Partition from, Partition to) {
		this.from = from;
		this.to = to;
	}

	@Arg(0)
	public Partition getFrom() {
		return from;
	}

	public void setFrom(Partition from) {
		this.from = from;
	}

	@Arg(1)
	public Partition getTo() {
		return to;
	}

	public void setTo(Partition to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.from);
		hash = 23 * hash + Objects.hashCode(this.to);
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
		final QuotientEdge other = (QuotientEdge) obj;
		if (!Objects.equals(this.from, other.from)) {
			return false;
		}
		if (!Objects.equals(this.to, other.to)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "QuotientEdge{" + "from=" + from + ", to=" + to + '}';
	}

}