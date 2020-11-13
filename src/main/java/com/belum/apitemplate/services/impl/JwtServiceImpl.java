package com.belum.apitemplate.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.belum.apitemplate.domain.ClientInfo;
import com.belum.apitemplate.services.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Created by bel-sahn on 7/31/19
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final Environment env;

    public JwtServiceImpl(Environment env) {
        this.env = env;
    }

    @Override
    public String createJwt(ClientInfo payload) throws UnsupportedEncodingException {
        Validate.notNull(payload);
        Validate.notBlank(payload.getClientId());
        Validate.notBlank(payload.getClientSecret());

        final String token = JWT.create()
                .withSubject(payload.getSubject())
                .withIssuer(payload.getIssuer())
                .withIssuedAt(Date.valueOf(LocalDate.now()))
                .withExpiresAt(Date.valueOf(LocalDate.now().plus(1L, ChronoUnit.DAYS)))
                .sign(getAlgorithm());

        log.info("Created Token :: {}", token);
        return token;
    }

    @Override
    public ClientInfo decodeJwt(String token) throws UnsupportedEncodingException {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        verifier.verify(token);

        final ClientInfo clientInfo = this.clientInfo(token);
        log.debug("Decoded JWT :: {}", clientInfo);
        return clientInfo;
    }

    private ClientInfo clientInfo(String token) {
        final DecodedJWT decodedJWT = JWT.decode(token);
        return new ClientInfo("",
                "",
                decodedJWT.getIssuer(),
                decodedJWT.getSubject(),
                decodedJWT.getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                decodedJWT.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256("SECRET");
    }
}
