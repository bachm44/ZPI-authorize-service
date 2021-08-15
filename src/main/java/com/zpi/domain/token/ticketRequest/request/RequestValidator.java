package com.zpi.domain.token.ticketRequest.request;

import com.zpi.domain.client.Client;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@NoArgsConstructor
@Component
public class RequestValidator {
    private static final HashSet<String> supportedResponseTypes = new HashSet<>(Collections.singleton("code"));
    private static final HttpStatus status = HttpStatus.FOUND;

    private Request request;
    private Client client;

    public void validate(Request request, Client client) throws InvalidRequestException {
        this.request = request;
        this.client = client;

        validateClient();
        validateRedirectUri();
        validateResponseType();
        validateRequiredParameters();
        validateScope();
    }

    private void validateClient() throws InvalidRequestException {
        if (isUnauthorizedClient()) {
            var error = RequestError.builder()
                    .error(RequestErrorType.UNAUTHORIZED_CLIENT)
                    .errorDescription("Unauthorized client id")
                    .build();
            throw new InvalidRequestException(status, error);
        }
    }

    private boolean isUnauthorizedClient() {
        return client == null;
    }

    private void validateRedirectUri() throws InvalidRequestException {
        if (isInvalidRedirectUri()) {
            var error = RequestError.builder()
                    .error(RequestErrorType.UNRECOGNIZED_REDIRECT_URI)
                    .errorDescription("Unrecognized redirect uri")
                    .build();
            throw new InvalidRequestException(status, error);
        }
    }

    private boolean isInvalidRedirectUri() {
        return !client.containsRedirectUri(request.getRedirectUri());
    }

    private void validateResponseType() throws InvalidRequestException {
        if (isUnsupportedResponseType()) {
            var error = RequestError.builder()
                    .error(RequestErrorType.UNSUPPORTED_RESPONSE_TYPE)
                    .errorDescription("Unrecognized response type: " + request.getResponseType())
                    .build();
            throw new InvalidRequestException(status, error);
        }
    }

    private boolean isUnsupportedResponseType() {
        var responseType = request.getResponseType();
        return responseType == null || !supportedResponseTypes.contains(responseType);
    }

    private void validateScope() throws InvalidRequestException {
        if (isScopeInvalid(request.getScope())) {
            var error = RequestError.builder()
                    .error(RequestErrorType.INVALID_SCOPE)
                    .errorDescription("Invalid scope")
                    .build();
            throw new InvalidRequestException(status, error);
        }
    }

    private static boolean isScopeInvalid(List<String> scope) {
        if (scope == null) {
            return true;
        }

        return !scope.contains("openid");
    }

    private void validateRequiredParameters() throws InvalidRequestException {
        var missing = missingRequiredParameters();

        if (missing.size() != 0) {
            var description = missingRequiredParametersDescription(missing);
            var error = RequestError.builder()
                    .error(RequestErrorType.INVALID_REQUEST)
                    .errorDescription(description)
                    .build();
            throw new InvalidRequestException(status, error);
        }
    }

    private List<String> missingRequiredParameters() {
        var result = new ArrayList<String>();

        if (request.getState() == null || request.getState().equals("")) {
            result.add("state");
        }

        if (request.getClientId() == null || request.getClientId().equals("")) {
            result.add("client_id");
        }

        return result;
    }

    private static String missingRequiredParametersDescription(List<String> missing) {
        return "Missing: " + String.join(" ", missing);
    }
}