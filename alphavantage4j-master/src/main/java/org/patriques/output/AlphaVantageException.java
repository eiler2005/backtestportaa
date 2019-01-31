package org.patriques.output;

public class AlphaVantageException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlphaVantageException(String message, Exception e) {
		super(message, e);
	}

	public AlphaVantageException(String message) {
		super(message);
	}
}
