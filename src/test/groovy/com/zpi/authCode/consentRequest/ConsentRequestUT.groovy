package com.zpi.authCode.consentRequest

import com.zpi.CommonFixtures
import com.zpi.domain.authCode.consentRequest.*
import com.zpi.domain.common.RequestError
import spock.lang.Specification
import spock.lang.Subject

class ConsentRequestUT extends Specification {
    def ticketRepository = Mock(TicketRepository)

    @Subject
    private ConsentServiceImpl service = new ConsentServiceImpl(ticketRepository)

    def "should return authentication code when ticket is in database"() {
        given:
            def request = CommonFixtures.consentRequest()
            def authData = TicketData.builder()
                    .redirectUri(CommonFixtures.redirectUri)
                    .build()

            def ticket = request.getTicket()

            ticketRepository.getByKey(ticket) >> Optional.of(authData)
            ticketRepository.remove(ticket) >> null
        when:
            def response = service.consent(request)

        then:
            1 * ticketRepository.remove(ticket)
        and:
            !response.getRedirectUri().isEmpty()
            !response.getCode().getValue().isEmpty()

            response.getState() == CommonFixtures.state
    }

    def "should return error when ticket was used before"() {
        given:
            def request = CommonFixtures.consentRequest()
            ticketRepository.getByKey(request.getTicket()) >> Optional.empty()

        when:
            service.consent(request)

        then:
            def thrownException = thrown(ErrorConsentResponseException)
            thrownException.getError() == Fixtures.error(request.getState())
    }

    private class Fixtures {
        static RequestError error(String state) {
            final ConsentErrorType errorType = ConsentErrorType.TICKET_EXPIRED
            final String description = "Ticket expired"

            return RequestError.builder()
                    .error(errorType)
                    .errorDescription(description)
                    .state(state)
                    .build()
        }
    }
}