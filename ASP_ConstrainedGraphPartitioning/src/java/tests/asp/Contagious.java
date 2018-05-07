package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;

@DefAtom("contagious")
public class Contagious {

	private Partition partition;

	public Contagious() {
	}

	public Contagious(Partition partition) {
		this.partition = partition;
	}

	@Arg(0)
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
		final Contagious other = (Contagious) obj;
		if (!Objects.equals(this.partition, other.partition)) {
			return false;
		}
	
		return true;
	}

	@Override
	public String toString() {
		return "Contagious{Partition=" +partition+"}";
	}

}