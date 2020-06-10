package com.drofff.checkers.server.deserializer;

import com.drofff.checkers.server.type.Mail;
import com.drofff.checkers.server.type.MailTemplates;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class MailTemplatesDeserializer extends StdDeserializer<MailTemplates> {

    public MailTemplatesDeserializer() {
        super(MailTemplates.class);
    }

    @Override
    public MailTemplates deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectNode objectNode = getObjectNode(jsonParser);
        MailTemplates mailTemplates = new MailTemplates();
        objectNode.fields().forEachRemaining(field -> {
            Mail mailTemplate = asMail(field.getValue());
            mailTemplates.put(field.getKey(), mailTemplate);
        });
        return mailTemplates;
    }

    private ObjectNode getObjectNode(JsonParser jsonParser) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        String jsonStr = jsonNode.asText();
        return (ObjectNode) new ObjectMapper().readTree(jsonStr);
    }

    private Mail asMail(JsonNode jsonNode) {
        Mail mail = new Mail();
        String title = jsonNode.get("title").asText();
        mail.setTitle(title);
        String text = jsonNode.get("text").asText();
        mail.setText(text);
        return mail;
    }

}