package br.com.flowopsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.mail.MailException;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher events;

    @Value("${flowops.mail.enabled}")
    private boolean enabled;

    @Value("${flowops.mail.from}")
    private String from;

    public void enviar(String destinatario, String assunto, String mensagem) {
        if (!enabled) {
            return;
        }
        events.publishEvent(new EmailSolicitado(destinatario, assunto, mensagem));
    }

    @Async("emailExecutor")
    @TransactionalEventListener
    public void enviarAposCommit(EmailSolicitado evento) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(evento.destinatario());
        email.setSubject(evento.assunto());
        email.setText(evento.mensagem());
        try {
            mailSender.send(email);
        } catch (MailException exception) {
            log.error("Falha no envio de notificação após o commit ({})", exception.getClass().getSimpleName());
        }
    }
}
