package exception;

/**
 * Custom exception for configuration errors
 */

public class SpaceBunnyConfigurationException extends Exception {
    public SpaceBunnyConfigurationException(String message){
        super(message);
    }

    public SpaceBunnyConfigurationException(Exception ex){
        super(ex.getMessage());
    }
}
