package mturchyn.blackwater.core.exceptions;

public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errorCode, message, cause, enableSuppression, writableStackTrace);
    }
}
