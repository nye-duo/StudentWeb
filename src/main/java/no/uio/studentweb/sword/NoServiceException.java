package no.uio.studentweb.sword;

public class NoServiceException extends DiscoveryException
{
	public NoServiceException()
	{
		super();
	}

	public NoServiceException(String message)
	{
		super(message);
	}

	public NoServiceException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoServiceException(Throwable cause)
	{
		super(cause);
	}
}
