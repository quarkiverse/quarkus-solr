package io.quarkiverse.solr.deployment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.beans.Field;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import io.quarkus.builder.item.SimpleBuildItem;

public final class SolrBeanInformationBuildItem extends SimpleBuildItem {
    private final Map<String, List<Map<String, String>>> solrBeans;

    public SolrBeanInformationBuildItem(Map<String, List<Map<String, String>>> solrBeans) {
        this.solrBeans = solrBeans;
    }

    public static SolrBeanInformationBuildItem collectSolrBeans(IndexView index) {
        Map<String, List<Map<String, String>>> solrBeans = index.getAnnotations(Field.class).stream()
                .map(i -> switch (i.target().kind()) {
                    case FIELD -> new BeanInformation(i.target().asField());
                    case METHOD -> new BeanInformation(i.target().asMethod());
                    default ->
                        throw new RuntimeException("Unsupported annotation target for @Field: " + i.target().kind());
                })
                .collect(Collectors.toMap(
                        x -> x.className,
                        x -> List.of(Map.of("javaFieldName", x.javaName, "solrName", x.solrName)),
                        (x, y) -> Stream.concat(x.stream(), y.stream()).toList()));
        return new SolrBeanInformationBuildItem(solrBeans);
    }

    public Map<String, List<Map<String, String>>> getSolrBeans() {
        return solrBeans;
    }

    private record BeanInformation(String className, String javaName, String solrName) {
        BeanInformation(FieldInfo fieldInfo) {
            this(getClassName(fieldInfo),
                    fieldInfo.name(),
                    getSolrName(fieldInfo));
        }

        BeanInformation(MethodInfo methodInfo) {
            this(getClassName(methodInfo),
                    methodInfo.name().substring(3), // Assuming getter method like getFieldName()
                    getSolrName(methodInfo));
        }

        private static String getSolrName(FieldInfo fieldInfo) {
            AnnotationValue value = fieldInfo.annotation(Field.class).value();
            if (value != null) {
                String s = value.asString();
                if (s != null && !s.isEmpty())
                    return s;
            }
            return fieldInfo.name();
        }

        private static String getSolrName(MethodInfo methodInfo) {
            AnnotationValue value = methodInfo.annotation(Field.class).value();
            if (value != null) {
                String s = value.asString();
                if (s != null && !s.isEmpty())
                    return s;
            }
            return methodInfo.name().substring(3); // Assuming getter method like getFieldName()
        }

        private static String getClassName(FieldInfo fieldInfo) {
            return fieldInfo.declaringClass().name().toString();
        }

        private static String getClassName(MethodInfo methodInfo) {
            return methodInfo.declaringClass().name().toString();
        }
    }
}
