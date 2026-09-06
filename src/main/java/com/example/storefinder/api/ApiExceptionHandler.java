package com.example.storefinder.api;

import com.example.storefinder.domain.StoreFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns a failed request into an RFC 9457 problem response.
 *
 * <p>The only class that logs per request, and only when a request fails.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(StoreFinderException.class)
    ProblemDetail handleStoreFinderException(StoreFinderException storeFinderFailure) {
        return reject(storeFinderFailure.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleUnreadableParameter(MethodArgumentTypeMismatchException unreadableParameter) {
        return reject("Parameter '" + unreadableParameter.getName() + "' must be a number");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail handleMissingParameter(MissingServletRequestParameterException missingParameter) {
        return reject("Parameter '" + missingParameter.getParameterName() + "' is required");
    }

    @ExceptionHandler(RuntimeException.class)
    ProblemDetail handleUnexpectedFailure(RuntimeException unexpectedFailure) {
        log.error("Request failed unexpectedly", unexpectedFailure);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "The request could not be completed");
        problemDetail.setTitle("Internal error");
        return problemDetail;
    }

    private static ProblemDetail reject(String reason) {
        log.warn("Rejected request: {}", reason);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, reason);
        problemDetail.setTitle("Invalid request");
        return problemDetail;
    }
}
