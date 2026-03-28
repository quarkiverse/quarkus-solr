# Quarkus Solr

[![Build](https://github.com/quarkiverse/quarkus-solr/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-solr/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.solr/quarkus-solr-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.quarkiverse.solr/quarkus-solr-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Integrates [Apache Solr](https://solr.apache.org/) into Quarkus for JDK and native builds.

## Features

- Solr as a dev-service for local development
- Provide [SolrJ](https://solr.apache.org/guide/solrj.html) beans for interacting with Solr
- Native compilation support
- Integration with Quarkus observability stack (health, metrics, logging)

## Getting Started

Add the following dependency to your project:

```xml

<dependency>
    <groupId>io.quarkiverse.solr</groupId>
    <artifactId>quarkus-solr</artifactId>
    <version>${latest.version}</version>
</dependency>
```

Then you can use SolrJ beans in your application to interact with Solr. For example:

```java
import org.apache.solr.client.solrj.SolrClient;

@ApplicationScoped
public class SolrService {
    @Inject
    SolrClient solrClient;

    public SolrDocumentList query(String query) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        return solrClient.query("collectionName", solrQuery).getResults();
    }
}
```

For dev-mode, a Solr instance will be automatically started and configured with a default collection called _dummy_. For
non-dev-mode, you have to configure the Solr client using the following property:

```properties
quarkus.solr.url=http://localhost:8983/solr
```

## Documentation

Check the [documentation](https://docs.quarkiverse.io/quarkus-solr/dev/index.html) for detailed information and usage
guidelines.

## Compatibility

Quarkus Solr has only been tested with Solr 10.0.0 and Quarkus 3.34.0, but other versions of Solr and Quarkus should
work as well.
If you encounter any issues, please report them in the issue tracker.

## Contributing

Feel free to contribute to this project by submitting issues or pull requests.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
