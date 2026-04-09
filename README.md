# Quarkus Solr

[![Build](https://github.com/quarkiverse/quarkus-solr/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-solr/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.solr/quarkus-solr-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.quarkiverse.solr/quarkus-solr-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A [Quarkus](https://quarkus.io/) extension that integrates [Apache Solr](https://solr.apache.org/) via SolrJ, with
dev services, Dev UI, MCP support, and full observability. The user documentation is available on the [Quarkiverse Hub](https://docs.quarkiverse.io/quarkus-solr/dev/index.html).

## Building

```bash
./mvnw clean install

# Native image build (requires Docker)
./mvnw -B install -Dnative -Dquarkus.native.container-build
```

## Contributing

Contributions are welcome. This project follows the
[Quarkiverse contribution guidelines](https://github.com/quarkiverse/quarkiverse/wiki/Becoming-a-Quarkiverse-member),
which require all CI checks to pass, code to be formatted with the project's code style, commits to be signed off
(DCO), and pull requests to include tests for new behaviour.

Please [open an issue](https://github.com/quarkiverse/quarkus-solr/issues) before starting significant work, so we can
align on the approach.

## Compatibility

This extension is built against Solr 10.0.0 and Quarkus 3.x. Other recent versions of Solr and Quarkus should work.
If you encounter compatibility problems, please [open an issue](https://github.com/quarkiverse/quarkus-solr/issues).

## Changelog

See [GitHub Releases](https://github.com/quarkiverse/quarkus-solr/releases) for the full changelog.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
