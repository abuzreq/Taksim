package asp;
import java.util.Objects;

import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefTerm;

@DefTerm("partition")
public class Partition {
	String id;
	public Partition() 
	{
	}
	public Partition(String id) {
		this.id = id;
	}
	
	public Partition(int i) {
		this(""+i);
	}
	@Arg(0)
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.id);
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
		final Partition other = (Partition) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Partition{" + "id=" + id + '}';
	}
}