package com.zpi.token.tokenRequest


import com.zpi.domain.authCode.consentRequest.authCodePersister.AuthCodeRepository
import com.zpi.domain.common.CodeGenerator
import com.zpi.domain.rest.ams.AmsService
import com.zpi.domain.rest.ams.AuthConfiguration
import com.zpi.domain.rest.ams.UserInfo
import com.zpi.domain.token.TokenRepository
import com.zpi.domain.token.TokenRequest
import com.zpi.domain.token.issuer.TokenIssuer
import com.zpi.domain.token.issuer.TokenIssuerImpl
import com.zpi.domain.token.issuer.config.TokenIssuerConfig
import com.zpi.domain.token.issuer.config.TokenIssuerConfigProvider
import com.zpi.token.TokenCommonFixtures
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class TokenIssuerUT extends Specification {
    def configProvider = Mock(TokenIssuerConfigProvider)
    def authCodeRepository = Mock(AuthCodeRepository)
    def tokenRepository = Mock(TokenRepository)
    def generator = Mock(CodeGenerator)
    def ams = Mock(AmsService)

    @Subject
    private TokenIssuer issuer = new TokenIssuerImpl(configProvider, authCodeRepository, tokenRepository, generator, ams)

    def "should return token when data correct"() {
        given:
            def request = TokenRequest.builder().code(TokenCommonFixtures.authCode.getValue()).build()

            def config = new TokenIssuerConfig(new AuthConfiguration(TokenCommonFixtures.secretKey, 1000L))

            ReflectionTestUtils.setField(config, "claims", TokenCommonFixtures.claims())
        and:
            generator.ticketCode() >> "fdsafdsa"
            configProvider.getConfig() >> config
            authCodeRepository.findByKey(TokenCommonFixtures.authCode.getValue()) >> Optional.of(TokenCommonFixtures.authCode)
            ams.userInfo(_) >> new UserInfo(TokenCommonFixtures.getUserData().getUsername(), List.of(""), List.of(""))

        when:
            def result = issuer.issue(request)

        then:
            !result.getAccessToken().isEmpty()

        and:
            def parsed = TokenCommonFixtures.parseToken(result.getAccessToken())
            parsed.getHeader().getAlgorithm() == TokenCommonFixtures.algorithm.getValue()

        and:
            def body = parsed.getBody()

            body.getIssuer() == "AUTH_SERVER"
            TokenCommonFixtures.areDatesQuiteEqual(body.getIssuedAt(), TokenCommonFixtures.claims().getIssuedAt())
            TokenCommonFixtures.areDatesQuiteEqual(body.getExpiration(), TokenCommonFixtures.claims().getExpirationTime())
            body.get("scope") == TokenCommonFixtures.authCode.getUserData().getScope()
            body.get("username") == TokenCommonFixtures.authCode.getUserData().getUsername()
    }
}
