package com.neolynks.worker.exception;

public class WorkerException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private interface ErrorCode {
       public int getCode();
       public String getErrorMsg();
	}

    public enum WORKER_CART_ERROR implements ErrorCode
    {
        UNKNOWN_CART_ID (1000, "Either cart doesn't exsit or already processed"),
        DUPLICATE_CART_ID (1001, "Already exist a cart with given id"),
        CART_CLOSED(1002, "Cart already closed, operation now allowed");
        private int code;
        private String errMsg;

        private WORKER_CART_ERROR(int code, String errMsg) {
            this.code = code;
            this.errMsg = errMsg;
        }

        public int getCode() {
            return code;
        }

        public String getErrorMsg() {
            return errMsg;
        }
    }

    public enum WORKER_SESSION_ERROR implements ErrorCode {

        UNKNOWN_SESSION_ID (1000, "Either worker session doesn't exsit or already terminated"),
        DUPLICATE_SESSION_ID (1001, "Already exist a cart with given id"),
        SESSION_PAUSED(1002, "Worker is in paused state, operation now allowed"),
        SESSION_CLOSED(1003, "Worker is closed, operation now allowed"),
        WORKER_OVERLOADED(1004, "Worker is overloaded"),
        INVALID_WORKER_TASK(1005, "Invalid worker task");;


        private int code;
        private String errMsg;

        private WORKER_SESSION_ERROR(int code, String errMsg) {
            this.code = code;
            this.errMsg = errMsg;
        }

        public int getCode() {
            return code;
        }

        public String getErrorMsg() {
            return errMsg;
        }
    }

    private ErrorCode errorCode;

    public WorkerException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode.getCode();
    }

    public String getErrorMsg() {
        return errorCode.getErrorMsg();
    }
}
