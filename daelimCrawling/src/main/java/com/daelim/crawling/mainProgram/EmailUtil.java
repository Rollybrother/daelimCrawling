package com.daelim.crawling.mainProgram;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("jinsoo4735@naver.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); 
            mailSender.send(message);
            System.out.println("메일이 성공적으로 전송되었습니다....");
        } catch (MessagingException e) {
            System.out.println("메일 전송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
