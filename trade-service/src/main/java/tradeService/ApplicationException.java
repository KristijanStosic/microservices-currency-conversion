package tradeService;

import org.springframework.http.HttpStatus;


@SuppressWarnings("serial")
public class ApplicationException extends RuntimeException {
	
    private String message;
    private HttpStatus httpStatus;

	public ApplicationException() {
		super();
	}
    
    
	public ApplicationException(String message, HttpStatus httpStatus) {
		super();
		this.message = message;
		this.httpStatus = httpStatus;
	}

	public String getMessage() {
		return message;
	}


	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
