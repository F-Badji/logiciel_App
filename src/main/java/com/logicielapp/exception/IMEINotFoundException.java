package com.logicielapp.exception;

/**
 * Exception levée quand un IMEI n'est pas trouvé dans les bases de données
 */
public class IMEINotFoundException extends Exception {
    
    public IMEINotFoundException(String message) {
        super(message);
    }
    
    public IMEINotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
