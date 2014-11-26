package mturchyn.blackwater.web;

public class Error {

    private String message;
    private String description;

    public static Error of(String message, String description) {
        Error error = new Error();
        error.message = message;
        error.description = description;
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
