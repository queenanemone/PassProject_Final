package com.tripboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 임시 비밀번호 이메일 발송 (비동기 처리)
     */
    @Async
    public void sendTempPasswordEmail(String toEmail, String tempPassword) {
        try {
            // 이메일 설정이 없으면 로그만 남기고 반환 (개발 환경용)
            if (fromEmail == null || fromEmail.isEmpty()) {
                log.warn("이메일 설정이 없어 이메일을 발송할 수 없습니다.");
                log.warn("임시 비밀번호 (개발용 - 서버 로그에서 확인): {}", tempPassword);
                log.warn("이메일 설정을 추가하거나, 서버 로그에서 임시 비밀번호를 확인하세요.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[Trip-Board] 임시 비밀번호 안내");
            message.setText(
                "안녕하세요, Trip-Board입니다.\n\n" +
                "비밀번호 재설정 요청에 따라 임시 비밀번호를 발급해드립니다.\n\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해주세요.\n\n" +
                "본인이 요청하지 않은 경우, 고객센터로 문의해주세요.\n\n" +
                "감사합니다."
            );

            mailSender.send(message);
            log.info("임시 비밀번호 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            // 이메일 발송 실패 시에도 로그에 임시 비밀번호 출력 (개발 환경용)
            log.warn("이메일 발송 실패로 인해 임시 비밀번호를 로그에 출력합니다 (개발용): {}", tempPassword);
            // 이메일 발송 실패해도 예외를 던지지 않고 로그만 남김
            // throw new RuntimeException("이메일 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

