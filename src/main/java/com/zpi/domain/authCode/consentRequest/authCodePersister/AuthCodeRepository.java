package com.zpi.domain.authCode.consentRequest.authCodePersister;

import com.zpi.domain.authCode.consentRequest.AuthCode;
import com.zpi.domain.common.EntityRepository;

public interface AuthCodeRepository extends EntityRepository<String, AuthCode> {
    void remove(String key);
}
