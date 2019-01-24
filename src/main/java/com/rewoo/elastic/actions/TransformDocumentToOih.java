package com.rewoo.elastic.actions;

import com.rewoo.elastic.util.JsonHelper;
import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.Date;

import static com.rewoo.elastic.util.JsonHelper.STANDARD_FORMATTER;

public class TransformDocumentToOih implements Module {
    private static final Logger logger = LoggerFactory.getLogger(TransformDocumentToOih.class);

    @Override
    public void execute(ExecutionParameters parameters) {
        JsonObject file = parameters.getMessage().getBody();
        logger.info("Try to transform file to OIH format");
        JsonObjectBuilder builder = Json.createObjectBuilder();
        long attachmentId = file.getJsonNumber("id").longValue();
        logger.info("File has been identified as attachment with id " + attachmentId);
        String attachmentUid = "attachment-" + attachmentId;
        addOihProperties(builder, attachmentUid);
        builder.add("name", file.getString("name"));
        builder.add("description", "");
        builder.add("baseType", file.getString("type"));
        builder.add("parentUid", "element-" + file.getJsonNumber("elementId").longValue());
        builder.add("path", file.getString("elementName"));
        builder.add("metadata", Json.createObjectBuilder().add("entryId", file.getJsonNumber("entryId").longValue())
                                                                .add("elementId", file.getJsonNumber("elementId").longValue()));
        addFileVersion(builder, file, attachmentUid);
        logger.info("Transformed file into OIH format");
        logger.info("Trying to emit file in OIH format to the platform");
        EventEmitter eventEmitter = parameters.getEventEmitter();
        final Message data = new Message.Builder().body(builder.build()).build();
        eventEmitter.emitData(data);
        logger.info("File emitted to the platform");
    }

    private static void addFileVersion(JsonObjectBuilder fileBuilder, JsonObject file, String attachmentUid) {
        JsonObjectBuilder versionBuilder = Json.createObjectBuilder();
        versionBuilder.add("label", "");
        versionBuilder.add("comment", "");
        versionBuilder.add("creation", JsonHelper.createModificationObject(file.getJsonNumber("authorId").longValue(),
                                                                                 new Date(file.getJsonNumber("creationTimestamp").longValue()),
                                                                                STANDARD_FORMATTER));
        versionBuilder.add("isLatestVersion", true);
        versionBuilder.add("isMajorVersion", true);
        versionBuilder.add("size", file.getJsonNumber("size"));
        versionBuilder.add("mimeType", file.getString("mimeType"));
        versionBuilder.add("url", file.getString("url"));
        versionBuilder.add("uid", attachmentUid);
        versionBuilder.add("type", file.getString("type"));
        versionBuilder.add("extension", file.getString("extension"));
        fileBuilder.add("currentVersion", versionBuilder.build());
    }

    private static void addOihProperties(JsonObjectBuilder fileBuilder, String attachmentUid) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        fileBuilder.add("oihUid", "");
        fileBuilder.addNull("oihCreated");
        fileBuilder.addNull("oihLastModified");
        fileBuilder.add("oihApplicationRecords", arrayBuilder.add(
                                 Json.createObjectBuilder().add("applicationUid", "")
                                                           .add("recordUid", attachmentUid)));
    }
}
