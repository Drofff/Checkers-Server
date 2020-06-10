package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.type.Mail;
import com.drofff.checkers.server.type.MailTemplates;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static com.drofff.checkers.server.utils.FileUtils.readFileFromClasspathAsStr;
import static com.drofff.checkers.server.utils.FormattingUtils.putParamsIntoText;

public class MailUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String MAILS_FILE = "mails.json";

    private MailUtils() {}

    public static Mail getActivationMail(String nickname, String token) {
        return getMailOfTemplateWithParams("account-activation",
                "nickname", nickname, "token", token);
    }

    private static Mail getMailOfTemplateWithParams(String templateKey, String ... params) {
        Mail mailTemplate = getMailTemplateByKey(templateKey);
        String mailTextTemplate = mailTemplate.getText();
        String mailText = putParamsIntoText(mailTextTemplate, params);
        mailTemplate.setText(mailText);
        return mailTemplate;
    }

    private static Mail getMailTemplateByKey(String templateKey) {
        String mailsJson = readFileFromClasspathAsStr(MAILS_FILE);
        MailTemplates mailTemplates = OBJECT_MAPPER.convertValue(mailsJson, MailTemplates.class);
        Mail mailTemplate = mailTemplates.get(templateKey);
        return Optional.ofNullable(mailTemplate)
                .orElseThrow(() -> new ValidationException("Missing mail template with key " + templateKey));
    }

}