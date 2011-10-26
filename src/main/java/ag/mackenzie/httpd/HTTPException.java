package ag.mackenzie.httpd;

public class HTTPException extends Exception {
	
	private HTTPResponseCode code;
	private String extraMessage;

	public HTTPException(HTTPResponseCode code, String extraMessage) {
		this.code = code;
		this.extraMessage = extraMessage;
	}
	
	@Override
	public String getMessage() {
		return "<html><head><title>" + code.message() 
		+ "</title></head><body><h1>" + code.code() + " " + code.message() 
		+ "</h1><p>" + this.extraMessage + "</p></body></html>";
	}

	public HTTPResponseCode getResponseCode() {
		return this.code;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
