package com.rewoo.elastic.actions;

import com.rewoo.elastic.util.JsonHelper;
import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Date;

import static com.rewoo.elastic.util.JsonHelper.STANDARD_FORMATTER;

public class TransformDocumentFromOih implements Module {
    private static final Logger logger = LoggerFactory.getLogger(TransformDocumentFromOih.class);

    @Override
    public void execute(ExecutionParameters parameters) {
        JsonObject file = parameters.getMessage().getBody();
        logger.info("Try to transform file to REWOO scope format");
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObject fileVersion = file.getJsonObject("currentVersion");
        builder.add("elementName", file.getString("path"));
        builder.add("url", fileVersion.getString("url"));
        builder.add("type", fileVersion.getString("type"));
        builder.add("size", fileVersion.getJsonNumber("size").longValue());
        builder.add("name", file.getString("name"));
        builder.add("mimeType", fileVersion.getString("mimeType"));
        builder.add("id", extractAttachmentId(fileVersion));
        builder.add("hash", extractHash(fileVersion));
        builder.add("extension", fileVersion.getString("extension"));
        builder.add("entryId", file.getJsonObject("metadata").getJsonNumber("entryId").longValue());
        builder.add("elementId", file.getJsonObject("metadata").getJsonNumber("elementId").longValue());
        Date creationTimestamp = JsonHelper.getModificationTimestamp(fileVersion.get("creation"), STANDARD_FORMATTER);
        if (creationTimestamp == null) {
            builder.addNull("creationTimestamp");
        } else {
            builder.add("creationTimestamp", creationTimestamp.toInstant().toEpochMilli());
        }
        Long authorId = JsonHelper.getAuthorId(fileVersion.get("creation"));
        if (authorId == null) {
            builder.addNull("authorId");
        } else {
            builder.add("authorId", authorId);
        }
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(builder);
        logger.info("Transformed file into REWOO scope format");
        logger.info("Trying to emit file in REWOO scope format to the platform");
        EventEmitter eventEmitter = parameters.getEventEmitter();
        final Message data = new Message.Builder().body(Json.createObjectBuilder().add("files", arrayBuilder).build()).build();
        eventEmitter.emitData(data);
        logger.info("File (as list with one item) emitted to the platform");
    }

    private static long extractAttachmentId(JsonObject oihDocumentVersion) {
        String uidString = oihDocumentVersion.getString("uid");
        return Long.valueOf(uidString.split("-")[1]);
    }

    private static String extractHash(JsonObject oihDocumentVersion) {
        String[] urlParts = oihDocumentVersion.getString("url").split("/");
        return urlParts[urlParts.length - 1];
    }
}
