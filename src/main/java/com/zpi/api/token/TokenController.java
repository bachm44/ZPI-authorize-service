package com.zpi.api.token;

import com.zpi.api.token.dto.RefreshRequestDTO;
import com.zpi.api.token.dto.TokenErrorResponseDTO;
import com.zpi.api.token.dto.TokenRequestDTO;
import com.zpi.api.token.dto.TokenResponseDTO;
import com.zpi.domain.token.TokenErrorResponseException;
import com.zpi.domain.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final TokenService service;

    @PostMapping
    public ResponseEntity<?> tokenRequest(@RequestBody TokenRequestDTO request) {
        try {
            var token = service.getToken(request.toDomain());
            return ResponseEntity.ok(new TokenResponseDTO(token));
        } catch (TokenErrorResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new TokenErrorResponseDTO(e.getResponse()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshRequest(@RequestBody RefreshRequestDTO request) {
        try {
            var token = service.refreshToken(request.toDomain());
            return ResponseEntity.ok(new TokenResponseDTO(token));
        } catch (TokenErrorResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new TokenErrorResponseDTO(e.getResponse()));
        }
    }
}
