package com.gdgoc.studyhub.domain.study.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * 스터디 초대 코드 생성기. 초대 코드를 알면 누구나 가입할 수 있으므로 추측하기 어려워야 한다.
 * Random 은 다음 값을 예측할 수 있어서, 보안용 난수인 SecureRandom 을 쓴다.
 */
@Component
public class InviteCodeGenerator {

    /** 헷갈리는 문자(0/O, 1/I/L)를 뺀 대문자 + 숫자 31자. 8자리면 약 8,500억 가지. */
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
