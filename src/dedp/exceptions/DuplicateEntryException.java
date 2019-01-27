package dedp.exceptions;

public class DuplicateEntryException extends Exception 
{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3090926470136086977L;


	public DuplicateEntryException()
	{
	}
	
	
	public DuplicateEntryException(String message)
	{
		super(message);
	}

}
