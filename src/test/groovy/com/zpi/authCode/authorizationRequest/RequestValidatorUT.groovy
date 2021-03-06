package com.zpi.authCode.authorizationRequest

import com.zpi.testUtils.CommonFixtures
import com.zpi.api.authCode.ticketRequest.TicketRequestDTO
import com.zpi.domain.authCode.authenticationRequest.AuthenticationRequestErrorType
import com.zpi.domain.authCode.authenticationRequest.OptionalParamsFiller
import com.zpi.domain.authCode.authenticationRequest.RequestValidatorImpl
import com.zpi.domain.authCode.authenticationRequest.ValidationFailedException
import com.zpi.domain.common.RequestError
import com.zpi.domain.rest.ams.AmsService
import com.zpi.domain.rest.ams.Client
import spock.lang.Specification
import spock.lang.Subject

class RequestValidatorUT extends Specification {
    def filler = Mock(OptionalParamsFiller)
    def ams = Mock(AmsService)

    @Subject
    private RequestValidatorImpl requestValidation = new RequestValidatorImpl(ams, filler)

    def "should not throw when all parameters correct"() {
        given:
            def request = CommonFixtures.request()
            def client = CommonFixtures.client()

            ams.clientDetails(request.getClientId()) >> Optional.of(client)
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            noExceptionThrown()
    }

    def "should throw unauthorized_client on non existing client"() {
        given:
            def request = Fixtures.correctRequest().toDomain()

            ams.clientDetails(request.getClientId()) >> Optional.empty()
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            def exception = thrown(ValidationFailedException)
            def expected = RequestError.builder()
                    .error(AuthenticationRequestErrorType.UNAUTHORIZED_CLIENT)
                    .errorDescription("Unauthorized client id")
                    .state(request.getState())
                    .build()

            exception.error == expected
    }

    def "should throw when incorrect redirect_uri"() {
        given:
            def request = Fixtures.requestWithCustomUri("UnrecognizedUri").toDomain()
            def client = CommonFixtures.client()

            ams.clientDetails(request.getClientId()) >> Optional.of(client)
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            def exception = thrown(ValidationFailedException)
            def expected = RequestError.builder()
                    .error(AuthenticationRequestErrorType.UNRECOGNIZED_REDIRECT_URI)
                    .errorDescription("Unrecognized redirect uri")
                    .state(request.getState())
                    .build()

            exception.error == expected
    }

    def "should return error message when client has no registered redirect uris"() {
        given:
            def request = Fixtures.requestWithCustomUri("UnrecognizedUri").toDomain()
            def client = Fixtures.clientWithEmptyRedirectURIs()

            ams.clientDetails(request.getClientId()) >> Optional.of(client)
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            def exception = thrown(ValidationFailedException)
            def expected = RequestError.builder()
                    .error(AuthenticationRequestErrorType.UNRECOGNIZED_REDIRECT_URI)
                    .errorDescription("Unrecognized redirect uri")
                    .state(request.getState())
                    .build()

            exception.error == expected
    }

    def "should throw invalid_request on missing required parameters"() {
        given:
            def client = CommonFixtures.client()

            ams.clientDetails(request.getClientId()) >> Optional.of(client)
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            def exception = thrown(ValidationFailedException)
            def expected = RequestError.builder()
                    .error(AuthenticationRequestErrorType.INVALID_REQUEST)
                    .errorDescription("Missing: " + errorDescription)
                    .state(request.getState())
                    .build()

            exception.error == expected

        where:
            request                            | _ || errorDescription
            Fixtures.nullClientId().toDomain() | _ || "client_id"
            Fixtures.nullState().toDomain()    | _ || "state"
    }

    def "should throw unsupported_response_type on wrong responseType"() {
        given:
            def client = CommonFixtures.client()

            ams.clientDetails(request.getClientId()) >> Optional.of(client)
            filler.fill(request) >> request

        when:
            requestValidation.validateAndFillMissingFields(request)

        then:
            def exception = thrown(ValidationFailedException)
            def expected = RequestError.builder()
                    .error(AuthenticationRequestErrorType.UNSUPPORTED_RESPONSE_TYPE)
                    .errorDescription(errorDescription)
                    .state(request.getState())
                    .build()

            exception.error == expected

        where:
            request                                   | _ || errorDescription
            Fixtures.invalidResponseType().toDomain() | _ || "Unrecognized response type: invalid"
    }

    private class Fixtures {
        static TicketRequestDTO correctRequest() {
            return TicketRequestDTO.builder()
                    .clientId(CommonFixtures.clientId)
                    .redirectUri(CommonFixtures.redirectUri)
                    .responseType(CommonFixtures.responseType)
                    .scope("phone%20photos%20asdf_asdf_asdf")
                    .state(CommonFixtures.state)
                    .build()
        }

        static TicketRequestDTO requestWithCustomUri(String uri) {
            return TicketRequestDTO.builder()
                    .clientId(CommonFixtures.clientId)
                    .redirectUri(uri)
                    .responseType(CommonFixtures.responseType)
                    .scope("profile")
                    .state(CommonFixtures.state)
                    .build()
        }

        static TicketRequestDTO nullClientId() {
            return TicketRequestDTO.builder()
                    .clientId(null)
                    .redirectUri(CommonFixtures.redirectUri)
                    .responseType(CommonFixtures.responseType)
                    .scope("profile")
                    .state(CommonFixtures.state)
                    .build()
        }

        static TicketRequestDTO nullState() {
            return TicketRequestDTO.builder()
                    .clientId(CommonFixtures.clientId)
                    .redirectUri(CommonFixtures.redirectUri)
                    .responseType(CommonFixtures.responseType)
                    .scope(CommonFixtures.scope)
                    .state(null)
                    .build()
        }

        static TicketRequestDTO invalidResponseType() {
            return TicketRequestDTO.builder()
                    .clientId(CommonFixtures.clientId)
                    .redirectUri(CommonFixtures.redirectUri)
                    .responseType("invalid")
                    .scope(CommonFixtures.scope)
                    .state(CommonFixtures.state)
                    .build()
        }

        static Client clientWithEmptyRedirectURIs() {
            return new Client(new ArrayList(),CommonFixtures.clientId);
        }
    }
}
