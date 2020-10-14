package ru.backtesting.rebalancing;

public enum Frequency {
	Annually("Annually"), 
	SemiAnnually("Quarterly"), 
	Quarterly("Quarterly"), 
	Monthly("Monthly"), 
	Weekly("Weekly"),
	Daily("Daily");
	
	final String value;

	Frequency(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
