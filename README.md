# OIH Transformer for REWOO Scope

The low code platform [REWOO Scope](https://rewoo.de) can be used to develop software to formalize business processes. 
In order to integrate these individual solutions into the company's IT landscape, the software can be 
coupled to other business software via the OIH using the Scope-to-OIH connector.
 
This connector consists of two parts: an [adapter](https://github.com/openintegrationhub/rewoo-adapter) and this transformer.
The adapter connects to the Scope [REST API](https://rewoo.de/manual/T-rest-api.html) to read and write data as JSON. The transformer transforms the Scope JSON format to OIH JSON and vice versa.

This transformer can be used to transform file data.

## Scope API

Changed files can be queried with method **getChangedFiles**. This transformer calls this method internally so you don't have to worry about the details. 
For a more in-depth look up the format in the [Scope manual](https://rewoo.de/manual/T-rest-api.html#ge%C3%A4nderte-dateien-abfragen).

## Actions

### Transform to OIH

This action (TransformDocumentToOih.java) transforms the Scope JSON object describing a file to the corresponding OIH object. No further configuration is needed.

### Transform from OIH

This action (TransformDocumentFromOih.java) transforms an OIH file object to a valid Scope JSON object. No further configuration is needed.  