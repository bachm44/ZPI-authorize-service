package com.zpi.domain.authCode.consentRequest;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class AuthCode {
    private final String value;
    private final AuthUserData userData;
}
