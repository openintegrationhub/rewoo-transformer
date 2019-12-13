/*
 *     Copyright 2019 REWOO Technologies AG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

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

public class TransformDocumentFromOih implements Module {
    private static final Logger logger = LoggerFactory.getLogger(TransformDocumentFromOih.class);

    @Override
    public void execute(ExecutionParameters parameters) {
        JsonObject file = parameters.getMessage().getBody();
        logger.info("Try to transform file to REWOO scope format");
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        if (file.containsKey("data")) {
            file = file.getJsonObject("data");
        }
        final JsonObject fileVersion = file.getJsonObject("currentVersion");
        builder.add("elementName", file.getString("path"));
        builder.add("url", fileVersion.getString("url"));
        builder.add("type", fileVersion.getString("type"));
        builder.add("size", fileVersion.getJsonNumber("size").longValue());
        builder.add("name", file.getString("name"));
        builder.add("mimeType", fileVersion.getString("mimeType"));
        builder.add("id", extractAttachmentId(fileVersion));
        builder.add("hash", extractHash(fileVersion));
        builder.add("extension", fileVersion.getString("extension"));
        if (file.containsKey("metadata")) {
            JsonObject metadata = file.getJsonObject("metadata");
            builder.add("entryId", metadata.containsKey("entryId") ? metadata.getJsonNumber("entryId").longValue() : -1);
            builder.add("elementId", metadata.containsKey("elementId") ? metadata.getJsonNumber("elementId").longValue() : -1);
        } else {
            builder.add("entryId", -1);
            builder.add("elementId", -1);
        }
        if (fileVersion.containsKey("creation")) {
            JsonValue creation = fileVersion.get("creation");
            final Date creationTimestamp = JsonHelper.getModificationTimestamp(creation, STANDARD_FORMATTER);
            if (creationTimestamp == null) {
                builder.addNull("creationTimestamp");
            } else {
                builder.add("creationTimestamp", creationTimestamp.toInstant().toEpochMilli());
            }
            Long authorId = JsonHelper.getAuthorId(creation);
            if (authorId == null) {
                builder.addNull("authorId");
            } else {
                builder.add("authorId", authorId);
            }
        } else {
            builder.addNull("creationTimestamp");
            builder.addNull("authorId");
        }
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(builder);
        logger.info("Transformed file into REWOO scope format");
        logger.info("Trying to emit file in REWOO scope format to the platform");
        EventEmitter eventEmitter = parameters.getEventEmitter();
        final Message data = new Message.Builder().body(Json.createObjectBuilder().add("files", arrayBuilder).build()).build();
        eventEmitter.emitData(data);
        logger.info("File (as list with one item) emitted to the platform");
    }

    private static long extractAttachmentId(JsonObject oihDocumentVersion) {
        final String uidString = oihDocumentVersion.getString("uid");
        return uidString.hashCode();
    }

    private static String extractHash(JsonObject oihDocumentVersion) {
        final String[] urlParts = oihDocumentVersion.getString("url").split("/");
        return urlParts[urlParts.length - 1];
    }
}
