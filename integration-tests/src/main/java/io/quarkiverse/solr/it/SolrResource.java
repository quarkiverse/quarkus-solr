/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.solr.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;

@Path("/solr")
@ApplicationScoped
public class SolrResource {
    private static final Logger LOG = Logger.getLogger(SolrResource.class.getName());
    private static final String COLLECTION = "dummy";
    private final SolrClient solrClient;

    SolrResource(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @POST
    public void add(SolrDocument document) throws SolrServerException, IOException {
        try {
            solrClient.addBean(COLLECTION, document);
            solrClient.commit(COLLECTION);
        } catch (Exception e) {
            LOG.error("Could not add document", e);
            throw e;
        }
    }

    @GET
    public List<SolrDocument> get(@QueryParam("query") String query) throws Exception {
        if (query == null || query.isEmpty()) {
            query = "*:*";
        }
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.addField("*");
        QueryResponse response = solrClient.query(COLLECTION, solrQuery);
        return response.getBeans(SolrDocument.class);
    }
}
