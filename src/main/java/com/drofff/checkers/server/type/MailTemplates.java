package com.drofff.checkers.server.type;

import com.drofff.checkers.server.deserializer.MailTemplatesDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;

@JsonDeserialize(using = MailTemplatesDeserializer.class)
public class MailTemplates extends HashMap<String, Mail> {
}