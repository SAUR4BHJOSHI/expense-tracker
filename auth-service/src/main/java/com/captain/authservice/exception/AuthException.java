package com.captain.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public AuthException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static AuthException invalidCredentials() {
        return new AuthException("Email or password is incorrect", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    public static AuthException emailAlreadyExists() {
        return new AuthException("Email is already registered", HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }

    public static AuthException accountLocked() {
        return new AuthException("Account is locked", HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED");
    }

    public static AuthException invalidToken() {
        return new AuthException("Token is invalid or expired", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }

    public static AuthException tokenRevoked() {
        return new AuthException("Token has been revoked", HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED");
    }
}

