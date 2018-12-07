package ru.backtesting.port.metrics;

import java.time.LocalDateTime;
import java.time.Period;


public class Drawdown {
	private LocalDateTime startDate;
	
	private LocalDateTime endDate;
	
	private Period downsidePeriod;
	
	private LocalDateTime recoveryBy;

	private Period recoveryPeriod;
	
	private Period underwaterPeriod;
	
	private double drawdown;

}
