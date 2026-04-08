package io.quarkiverse.solr.it;

import org.apache.solr.client.solrj.beans.Field;

@SuppressWarnings("unused") //Used via reflection
public class SolrDocument {
    @Field
    public String id;

    @Field("firstName")
    public String firstName;

    @Field("lastName")
    public String lastName;

    @Field("description")
    public String description;

    @SuppressWarnings("unused") //Used internally by solr
    public SolrDocument() {
    }

    @SuppressWarnings("unused") //Used internally by Jackson
    public SolrDocument(String id, String firstName, String lastName, String description) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.description = description;
    }
}
