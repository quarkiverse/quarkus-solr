package io.quarkiverse.solr.deployment;

import io.quarkiverse.solr.runtime.SolrDevJsonRpcService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = { IsDevelopment.class })
public class DevUIProcessor {
    private static final String SOLR_LOGO = "solr.png";
    private static final String SOLR_GROUPID = "org.apache.solr";
    private static final String SOLR_ARTIFACTID = "solr-solrj";
    private static final String SOLR_NAME = "SolrJ";
    private static final String SOLR_LINK = "https://solr.apache.org/guide/solr/latest/deployment-guide/solrj.html";

    @BuildStep
    public CardPageBuildItem createCard() {
        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();
        cardPageBuildItem.addLibraryVersion(SOLR_GROUPID, SOLR_ARTIFACTID, SOLR_NAME, SOLR_LINK);
        cardPageBuildItem.setLogo(SOLR_LOGO, SOLR_LOGO);
        cardPageBuildItem.addPage(Page.externalPageBuilder("Solr Admin UI")
                .icon("font-awesome-solid:diagram-project")
                .dynamicUrlJsonRPCMethodName("getSolrAdminUrl")
                .doNotEmbed());
        return cardPageBuildItem;
    }

    @BuildStep
    JsonRPCProvidersBuildItem produceOidcDevJsonRpcService() {
        return new JsonRPCProvidersBuildItem(SolrDevJsonRpcService.class);
    }
}
