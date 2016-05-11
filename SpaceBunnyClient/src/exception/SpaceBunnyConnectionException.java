package exception;

public class SpaceBunnyConnectionException extends Exception {
    public SpaceBunnyConnectionException(String message){
        super(message);
    }

    public SpaceBunnyConnectionException(Exception ex){
        super(ex.getMessage());
    }
}
