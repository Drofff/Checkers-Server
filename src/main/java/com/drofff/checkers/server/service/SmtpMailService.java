package com.drofff.checkers.server.service;

import com.drofff.checkers.server.exception.CheckersServerException;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.type.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.drofff.checkers.server.utils.ValidationUtils.validateNotNull;

@Service
public class SmtpMailService implements MailService {

    private static final boolean IS_HTML_BODY = true;

    private final JavaMailSender javaMailSender;

    @Value("${mail.address}")
    private String mailAddress;

    public SmtpMailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public Mono<Void> sendMailTo(Mail mail, String ... receivers) {
        validateNotNull(mail, "Mail should not be null");
        validateReceivers(receivers);
        MimeMessage mimeMessage = buildMimeMessageOfMailForReceivers(mail, receivers);
        return Mono.just(mimeMessage).doOnNext(javaMailSender::send).then();
    }

    private void validateReceivers(String ... receivers) {
        if(receivers.length == 0) {
            throw new ValidationException("At least one receiver should be provided");
        }
    }

    private MimeMessage buildMimeMessageOfMailForReceivers(Mail mail, String ... receivers) {
        try {
            return mimeMessageOf(mail, receivers);
        } catch(MessagingException e) {
            throw new CheckersServerException(e.getMessage());
        }
    }

    private MimeMessage mimeMessageOf(Mail mail, String ... receivers) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom(mailAddress);
        mimeMessageHelper.setSubject(mail.getTitle());
        mimeMessageHelper.setText(mail.getText(), IS_HTML_BODY);
        mimeMessageHelper.setTo(receivers);
        return mimeMessage;
    }

}