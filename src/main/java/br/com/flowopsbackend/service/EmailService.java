package br.com.flowopsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${flowops.mail.enabled}")
    private boolean enabled;

    @Value("${flowops.mail.from}")
    private String from;

    public void enviar(String destinatario, String assunto, String mensagem) {
        if (!enabled) {
            return;
        }
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(destinatario);
        email.setSubject(assunto);
        email.setText(mensagem);
        mailSender.send(email);
    }
}
