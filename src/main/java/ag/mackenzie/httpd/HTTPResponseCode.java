package ag.mackenzie.httpd;

public enum HTTPResponseCode {
	OK (200, "OK"),
	FILE_NOT_FOUND(404, "File Not Found"),
	SERVER_ERROR(500, "Internal Server Error"),
	UNIMPLEMENTED(501, "Method Not Implemented"),
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden");
	
	
	private int code;
	private String message;

	HTTPResponseCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public int code() {
		return this.code;
	}
	
	public String message() {
		return this.message;
	}
	
	public String toString() {
		return "HTTP/1.1 " + this.code + " " + this.message;
	}
}
