package io.quarkiverse.solr.it;

import org.apache.solr.client.solrj.beans.Field;

@SuppressWarnings("unused") //Used via reflection
public class SolrDocument {
    @Field
    public String id;

    @Field("name_t")
    public String name;

    @Field("description_t")
    public String description;

    @SuppressWarnings("unused") //Used internally by solr
    public SolrDocument() {
    }

    @SuppressWarnings("unused") //Used internally by jackson
    public SolrDocument(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
