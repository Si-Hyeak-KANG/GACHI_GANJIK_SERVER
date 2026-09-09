package com.gachiganjik.gachiganjik_server.domain.user.service;

import com.gachiganjik.gachiganjik_server.common.exception.BusinessException;
import com.gachiganjik.gachiganjik_server.common.exception.ErrorCode;
import com.gachiganjik.gachiganjik_server.domain.user.entity.EmailVerificationCode;
import com.gachiganjik.gachiganjik_server.domain.user.entity.VerifiedEmail;
import com.gachiganjik.gachiganjik_server.domain.user.repository.EmailVerificationRepository;
import com.gachiganjik.gachiganjik_server.domain.user.repository.VerifiedEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import com.gachiganjik.gachiganjik_server.domain.user.entity.LoginType;
import com.gachiganjik.gachiganjik_server.domain.user.repository.UserLoginInfoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String DIGITS = "0123456789";
    private static final int CODE_LENGTH = 6;

    @Value("${email.verification.code-expiration-seconds}")
    private long codeExpirationSeconds;

    @Value("${email.verification.verified-ttl-seconds}")
    private long verifiedTtlSeconds;

    private final EmailVerificationRepository emailVerificationRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;
    private final UserLoginInfoRepository userLoginInfoRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public void sendCode(String email) {
        if (userLoginInfoRepository.existsByEmailAndLoginType(email, LoginType.EMAIL)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String code = generateCode();
        emailVerificationRepository.save(new EmailVerificationCode(email, code, codeExpirationSeconds));

        if ("prod".equals(activeProfile)) {
            sendEmail(email, code);
        } else {
            log.info("[EMAIL VERIFICATION] email={} code={}", email, code);
        }
    }

    public void verifyCode(String email, String code) {
        EmailVerificationCode saved = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (!saved.getCode().equals(code)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_INVALID);
        }

        emailVerificationRepository.deleteById(email);

        // 인증 사실을 남긴다. 회원가입이 이 마커를 확인·소비한다.
        verifiedEmailRepository.save(new VerifiedEmail(email, verifiedTtlSeconds));
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[같이간직] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n3분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return sb.toString();
    }
}