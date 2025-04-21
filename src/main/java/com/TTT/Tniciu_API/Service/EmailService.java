package com.TTT.Tniciu_API.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token, String email) throws MessagingException {
        String subject = "Xác nhận đăng ký tài khoản - Bean Farm";
        String verificationLink = "http://localhost:8080/verify?token=" + token + "&email=" + email;
        String content = "<html>"
                + "<body>"
                + "<h1>Xác nhận đăng ký tài khoản</h1>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại Bean Farm. Vui lòng nhấn vào đường link dưới đây để kích hoạt tài khoản của bạn:</p>"
                + "<a href=\"" + verificationLink + "\">Kích hoạt tài khoản</a>"
                + "<p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>"
                + "</body>"
                + "</html>";

        sendEmail(to, subject, content);
    }

    public void sendVerificationSuccessEmail(String to) throws MessagingException {
        String subject = "Kích hoạt tài khoản thành công - Bean Farm";
        String content = "<html>"
                + "<body>"
                + "<h1>Kích hoạt tài khoản thành công</h1>"
                + "<p>Tài khoản của bạn đã được kích hoạt thành công trên Bean Farm.</p>"
                + "<p>Bây giờ bạn có thể đăng nhập và sử dụng dịch vụ của chúng tôi.</p>"
                + "</body>"
                + "</html>";

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }
}
