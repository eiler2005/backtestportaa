package ru.backtesting.rebalancing;

public class RebalancingType {
	private Frequency frequency;
	private RebalancingMethod method;
	
	public RebalancingType(Frequency frequency, RebalancingMethod method) {
		super();
		this.frequency = frequency;
		this.method = method;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public RebalancingMethod getRebalMethod() {
		return method;
	}

}
