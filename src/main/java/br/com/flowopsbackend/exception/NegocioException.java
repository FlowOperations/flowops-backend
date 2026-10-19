package br.com.flowopsbackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
