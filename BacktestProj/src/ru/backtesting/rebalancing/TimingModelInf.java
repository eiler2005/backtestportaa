package ru.backtesting.rebalancing;

public class TimingModelInf {
	private Frequency frequency;
	private TimingModelType method;
	
	public TimingModelInf(Frequency frequency, TimingModelType method) {
		super();
		this.frequency = frequency;
		this.method = method;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public TimingModelType getMethod() {
		return method;
	}

}
