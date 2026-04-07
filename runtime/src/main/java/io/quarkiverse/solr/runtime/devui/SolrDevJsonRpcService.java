package io.quarkiverse.solr.runtime.devui;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import io.quarkiverse.solr.runtime.SolrRunTimeConfig;
import io.quarkus.runtime.annotations.DevMCPEnableByDefault;
import io.quarkus.runtime.annotations.JsonRpcDescription;
import io.smallrye.common.annotation.NonBlocking;

public class SolrDevJsonRpcService {
    private static final Logger Log = Logger.getLogger(SolrDevJsonRpcService.class.getName());
    private final SolrClient solrClient;
    private final SolrRunTimeConfig solrRunTimeConfig;

    public SolrDevJsonRpcService(SolrClient solrClient, SolrRunTimeConfig solrRunTimeConfig) {
        this.solrClient = solrClient;
        this.solrRunTimeConfig = solrRunTimeConfig;
    }

    @JsonRpcDescription("Get solr URL")
    @DevMCPEnableByDefault
    @NonBlocking
    public String getSolrAdminUrl() {
        Log.fine("Get Solr Admin URL");
        return solrRunTimeConfig.url().get(0);
    }

    @JsonRpcDescription("List solr collections")
    @DevMCPEnableByDefault
    public List<String> listCollections() throws SolrServerException, IOException {
        CollectionAdminRequest.List request = new CollectionAdminRequest.List();
        Log.fine(() -> "Getting all collection");
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) request.process(solrClient).getResponse().get("collections");
        return result;
    }

    @JsonRpcDescription("Get schema for a Solr collection")
    @DevMCPEnableByDefault
    public SchemaRepresentation getSchema(@JsonRpcDescription("Solr collection to get schema for") String collection)
            throws SolrServerException, IOException {
        Log.fine(() -> "Getting schema for collection " + collection);
        SchemaRequest schemaRequest = new SchemaRequest();
        return schemaRequest.process(solrClient, collection).getSchemaRepresentation();
    }

    @JsonRpcDescription("Index documents from a JSON string into the solr collection")
    @DevMCPEnableByDefault
    public UpdateResponse indexJsonDocuments(@JsonRpcDescription("Solr collection to index into") String collection,
            @JsonRpcDescription("Documents to index, as JSON") String json) throws SolrServerException, IOException {
        for (SolrInputDocument doc : parseInputDocuments(json)) {
            Log.info(() -> "Adding document to collection " + collection + ": " + doc);
            solrClient.add(collection, doc);
        }
        return solrClient.commit(collection);
    }

    @JsonRpcDescription("Search specified Solr collection with a query, optional filters, facets, sorting and pagination.")
    @DevMCPEnableByDefault
    public SolrDocumentList search(@JsonRpcDescription("Solr collection to query") String collection,
            @JsonRpcDescription("Solr q parameter. If none specified defaults to \"*:*\"") String query,
            @JsonRpcDescription("Solr fq parameter. Either a string (if just one value), or a JSON-Array of strings if multiple values.") String fq,
            @JsonRpcDescription("Solr sort parameter and their order. A JSON object with field names as keys and sort order (asc/desc) as values\");") String sortClauses,
            @JsonRpcDescription("Starting offset for pagination") Integer start,
            @JsonRpcDescription("Number of rows to return") Integer rows) throws SolrServerException, IOException {
        Log.fine(() -> "Querying " + query);
        SolrQuery solrQuery = new SolrQuery("*:*");
        if (query != null && !query.isEmpty())
            solrQuery.setQuery(query);
        if (fq != null && !fq.isEmpty())
            solrQuery.setFilterQueries(parseFilterQueries(fq));
        if (start != null)
            solrQuery.setStart(start);
        if (rows != null)
            solrQuery.setRows(rows);
        if (sortClauses != null && !sortClauses.isEmpty())
            solrQuery.setSorts(parseSortClauses(sortClauses));
        return solrClient.query(collection, solrQuery).getResults();
    }

    private List<SolrInputDocument> parseInputDocuments(String input) {
        try (JsonReader reader = Json.createReader(new StringReader(input))) {
            JsonStructure json = reader.read();
            if (json instanceof JsonObject jsonObject)
                return List.of(parseInputDocument(jsonObject));
            if (json instanceof JsonArray jsonArray)
                return parseInputDocuments(jsonArray);
        }
        throw new RuntimeException("Invalid JSON input for Solr document. Must be a JSON object, or an array of JSON objects.");
    }

    private SolrInputDocument parseInputDocument(JsonObject jsonObject) {
        SolrInputDocument doc = new SolrInputDocument();
        jsonObject.keySet().forEach(key -> {
            JsonValue value = jsonObject.get(key);
            if (value instanceof JsonString jsonString) {
                doc.addField(key, jsonString.getString());
            } else if (value instanceof JsonArray jsonArray) {
                List<String> values = jsonArray.stream()
                        .filter(JsonString.class::isInstance)
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .toList();
                doc.addField(key, values);
            } else {
                doc.addField(key, value.toString());
            }
        });
        return doc;
    }

    private List<SolrInputDocument> parseInputDocuments(JsonArray jsonArray) {
        if (jsonArray.stream().anyMatch(e -> !(e instanceof JsonObject))) {
            throw new RuntimeException(
                    "Invalid JSON input for Solr document. When providing an array, all elements must be JSON objects.");
        }
        return jsonArray.stream()
                .map(JsonObject.class::cast)
                .map(this::parseInputDocument)
                .toList();
    }

    private String[] parseFilterQueries(String input) {
        try (JsonReader reader = Json.createReader(new StringReader(input))) {
            JsonStructure json = reader.read();
            if (json instanceof JsonArray jsonArray) {
                return jsonArray.stream()
                        .map(JsonValue::toString)
                        .toArray(String[]::new);
            }
            throw new RuntimeException("Invalid JSON input for fq query. Must be an array of strings.");
        } catch (JsonException e) {
            //This is not json, so just use the full query
            return new String[] { input };
        }
    }

    private List<SolrQuery.SortClause> parseSortClauses(String input) {
        try (JsonReader reader = Json.createReader(new StringReader(input))) {
            JsonStructure json = reader.read();
            if (json instanceof JsonObject jsonObject) {
                return jsonObject.keySet().stream()
                        .map(e -> new SolrQuery.SortClause(e, jsonObject.getString(e)))
                        .toList();
            }
        }
        throw new RuntimeException(
                "Invalid JSON input for sort query. Must be an object with field names as keys and sort order (asc/desc) as values.");
    }
}
