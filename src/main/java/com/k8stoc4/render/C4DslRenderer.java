package com.k8stoc4.render;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import com.k8stoc4.model.*;

import java.io.StringWriter;
import java.util.*;

@Slf4j
public class C4DslRenderer {

    private static final MustacheFactory MF = new DefaultMustacheFactory();

    public Output render(final C4Model model) {
        return new Output(renderModel(model), renderSpec(model));
    }

    // Render principale: workspace
    private String renderModel(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append("{\n");
        sb.append(renderClusterScoped(model));
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(renderNamespace(namespace));
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderClusterScoped(C4Model model) {
        if (model.getClusterScopedComponents() == null || model.getClusterScopedComponents().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("  // Cluster-scoped resources\n");

        List<Map<String, Object>> comps = model.getClusterScopedComponents().stream()
                .map(c -> {
                    Map<String, Object> modelData = new HashMap<>();
                    modelData.put("kind", c.getKind().toLowerCase());
                    modelData.put("id", c.getId().replace(".", "-"));
                    modelData.put("name", c.getName());
                    modelData.put("technology", c.getKind());
                    modelData.put("description", c.getDescription());

                    modelData.put(
                            "labels",
                            Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                                    .map(e -> Map.of(
                                            "key", e.getKey(),
                                            "value", e.getValue()
                                    ))
                                    .toList()
                    );

                    modelData.put(
                            "annotations",
                            Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                                    .map(e -> Map.of(
                                            "key", e.getKey(),
                                            "value", e.getValue()
                                    ))
                                    .toList()
                    );

                    return modelData;
                })
                .toList();

        for (Map<String, Object> comp : comps) {
            sb.append("  ").append(comp.get("kind")).append(" ").append(comp.get("id")).append(" '").append(comp.get("name")).append("' {\n");
            sb.append("    technology \"").append(comp.get("technology")).append("\"\n");
            sb.append("    description \"").append(comp.get("description")).append("\"\n");
            sb.append("  }\n");
        }

        sb.append("  // Cross-scope relationships\n");
        List<C4Relationship> modelRelationships = new ArrayList<>(model.getRelationships());
        sb.append("  // Total model relationships: ").append(modelRelationships.size()).append("\n");
        for (C4Relationship rel : modelRelationships) {
            sb.append("  ").append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    private String renderNamespace(C4Namespace namespace) {
        Mustache mustache = MF.compile("templates/namespace.mustache");

        List<Map<String, Object>> labelGroups = new ArrayList<>();
        for (C4LabelGroup lg : namespace.getLabelGroups()) {
            Map<String, Object> lgModel = new HashMap<>();
            lgModel.put("name", lg.getName().replace(".","-"));
            lgModel.put("components", lg.getComponents().stream()
                .map(c -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("kind", c.getKind().toLowerCase());
                    model.put("id", c.getId().replace(".", "-"));
                    model.put("name", c.getName());
                    model.put("technology", c.getKind());
                    model.put("description", c.getDescription());
                    model.put("labels", Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                            .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                            .toList());
                    model.put("annotations", Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                            .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                            .toList());
                    return model;
                })
                .toList());
            labelGroups.add(lgModel);
        }

        List<Map<String, Object>> comps =
                namespace.getComponents().stream()
                        .map(c -> {
                            Map<String, Object> model = new HashMap<>();

                            model.put("kind", c.getKind().toLowerCase());
                            model.put("id", c.getId().replace(".", "-"));
                            model.put("name", c.getName());
                            model.put("technology", c.getKind());
                            model.put("description", c.getDescription());

                            model.put(
                                    "labels",
                                    Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                                            .map(e -> Map.of(
                                                    "key", e.getKey(),
                                                    "value", e.getValue()
                                            ))
                                            .toList()
                            );

                            model.put(
                                    "annotations",
                                    Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                                            .map(e -> Map.of(
                                                    "key", e.getKey(),
                                                    "value", e.getValue()
                                            ))
                                            .toList()
                            );

                            return model;
                        })
                        .toList();


        List<String> relations = new ArrayList<>();

        for (C4Relationship rel : namespace.getRelationships()) {
            relations.add(rel.getSource() + " -> " + rel.getTarget()+" "+rel.getTag());
        }

        Map<String, Object> ctx = Map.of(
                "name", namespace.getName(),
                "labelGroups", labelGroups,
                "components", comps,
                "relations", relations
        );

        StringWriter writer = new StringWriter();
        mustache.execute(writer, ctx);
        return writer.toString();
    }

    public String renderRelations(C4Model model) {
        StringBuilder sb = new StringBuilder();
        for (C4Relationship rel: model.getRelationships()){
            sb.append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    private String renderSpec(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("specification ").append("{").append("\n");
        for (String elementName: model.getSpecifications()) {
            sb.append("    ").append("element").append(" ").append(elementName).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public static class Output {
        private final String model;
        private final String spec;

        private Output(String model, String spec) {
            this.model = model;
            this.spec = spec;
        }

        public String getModel() { return this.model; }

        public String getSpec() { return this.spec; }
    }

}
