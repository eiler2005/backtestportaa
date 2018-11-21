package ru.backtesting.rebalancing;

public class RebalancingBehavior {
	private RebalancingAction rebalancing;
	
	public RebalancingAction getRebalancing() {
		return rebalancing;
	}
	public void setRebalancing(RebalancingAction rebalancing) {
		this.rebalancing = rebalancing;
	}
	
	public int execute () {
		return rebalancing.execute();
	}
}
