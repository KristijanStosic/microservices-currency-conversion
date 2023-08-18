package bankAccount;

import java.time.LocalDateTime;

public class ApiErrorResponse {
	
	private String guid;
    private String message;
    private Integer statusCode;
    private String statusName;
    private String path;
    private String method;
    private LocalDateTime timestamp;
    
    public ApiErrorResponse() {
    	
    }
    
    public ApiErrorResponse(String guid) {
    	this.guid = guid;
    }
    
    public ApiErrorResponse(String guid, String message) {
    	this.guid = guid;
    	this.message = message;
    }
    
    public ApiErrorResponse(String guid, String message, Integer statusCode) {
    	this.guid = guid;
    	this.message = message;
    	this.statusCode = statusCode;
    }
    
    public ApiErrorResponse(String guid, String message, Integer statusCode, String statusName) {
    	this.guid = guid;
    	this.message = message;
    	this.statusCode = statusCode;
    	this.statusName = statusName;
    	
    }
    
    public ApiErrorResponse(String guid, String message, Integer statusCode, String statusName, String path) {
    	this.guid = guid;
    	this.message = message;
    	this.statusCode = statusCode;
    	this.statusName = statusName;
    	this.path = path;
    	
    }
    
    public ApiErrorResponse(String guid, String message, Integer statusCode, String statusName, String path, String method) {
    	this.guid = guid;
    	this.message = message;
    	this.statusCode = statusCode;
    	this.statusName = statusName;
    	this.path = path;
    	this.method = method;	
    }

	public ApiErrorResponse(String guid, String message, Integer statusCode, String statusName,
			String path, String method, LocalDateTime timestamp) {
		this.guid = guid;
		this.message = message;
		this.statusCode = statusCode;
		this.statusName = statusName;
		this.path = path;
		this.method = method;
		this.timestamp = timestamp;
	}

	public String getGuid() {
		return guid;
	}


	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Integer getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}


	public String getStatusName() {
		return statusName;
	}


	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getMethod() {
		return method;
	}


	public void setMethod(String method) {
		this.method = method;
	}


	public LocalDateTime getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
    
    
}
