package com.agimatec.commons.generator;

/**
 * Description: runtime exception during generators (template exception etc)<br>
 */
public class GeneratorException extends RuntimeException {
    public GeneratorException(String message) {
        super(message);
    }

    public GeneratorException(String message, Throwable cause) {

        super(message, cause);
    }

    public GeneratorException(Throwable cause) {
        super(cause);
    }
}
