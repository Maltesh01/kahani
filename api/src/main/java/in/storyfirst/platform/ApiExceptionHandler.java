package in.storyfirst.platform;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {
        var reason = exception.getReason() == null
                ? "The request could not be completed."
                : exception.getReason();
        var problem = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), reason);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
