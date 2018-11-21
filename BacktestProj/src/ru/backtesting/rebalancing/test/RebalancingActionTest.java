package ru.backtesting.rebalancing.test;

import ru.backtesting.rebalancing.ProportionRebalancing;
import ru.backtesting.rebalancing.RebalancingBehavior;

public class RebalancingActionTest {

	public static void main(String[] args) {
		RebalancingBehavior portAction = new RebalancingBehavior();
		
		portAction.setRebalancing(new ProportionRebalancing());
		
		int result = portAction.execute();
	}
}
