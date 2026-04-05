package io.quarkiverse.solr.deployment.devui;

import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;

@BuildSteps(onlyIf = { IsDevelopment.class })
public class DevAssistantProcessor {
    private static final String CREATE_QUERY = """
            Can you create a valid Solr query for the following description: {{description}}

            Reply with the valid query in the query field. Add an example in markdown format on how to use this with the in the example field.
            """;

    @BuildStep
    void createBuildTimeActions(BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer) {
        BuildTimeActionBuildItem bta = new BuildTimeActionBuildItem();
        bta.actionBuilder()
                .methodName("createQuery")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    return assistant.assistBuilder()
                            .userMessage(CREATE_QUERY)
                            .variables(p)
                            .responseType(QueryResponse.class)
                            .assist();
                }).build();
        buildTimeActionProducer.produce(bta);
    }

    record QueryResponse(String query, String example) {
    }
}
