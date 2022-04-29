package com.enterprise_architecture.essential.importutility.data.global;

public class PromoteRepositoryException extends Exception {
	private String errorMessage;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PromoteRepositoryException(String error) {
		errorMessage = error;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		if(errorMessage != null) {
			return errorMessage;
		} else {
			return super.getMessage();
		}
	}
	
	

}
