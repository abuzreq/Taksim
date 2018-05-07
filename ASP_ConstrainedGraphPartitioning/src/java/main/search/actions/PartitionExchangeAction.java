package search.actions;

import aima.core.agent.Action;
import search.basic.Partition;

public class PartitionExchangeAction implements Action {

	Partition p1,p2;
	public Partition getP1() {
		return p1;
	}

	public void setP1(Partition p1) {
		this.p1 = p1;
	}

	public Partition getP2() {
		return p2;
	}

	public void setP2(Partition p2) {
		this.p2 = p2;
	}

	public PartitionExchangeAction(Partition p1, Partition p2)
	{
		this.p1 = p1;
		this.p2 = p2;
	}

	@Override
	public boolean isNoOp() {
		// TODO Auto-generated method stub
		return false;		
	}
	
	@Override
	public boolean equals(Object ob)
	{
		if(ob instanceof PartitionExchangeAction)
		{
			PartitionExchangeAction other = (PartitionExchangeAction)ob;
			return (this.p1.equals(other.getP1()) && this.p2.equals(other.getP2())) || (this.p1.equals(other.getP2()) && this.p2.equals(other.getP1()));
		}
		else 
			return false;
	}
	@Override
	public int hashCode()
	{
		int n1 = this.p1.getNumber();
		int n2 = this.p2.getNumber();		
		return n1 <= n2? (n1+":"+n2).hashCode() : (n2+":"+n1).hashCode();
				
	}
	@Override
	public String toString() {
		return "[Swap labels of "+p1.getNumber()+ " and " +p2.getNumber()+"]";
	}

}
