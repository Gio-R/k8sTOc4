package render;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import model.*;

import java.io.StringWriter;
import java.util.*;

public class C4DslRenderer {

    private static final MustacheFactory MF = new DefaultMustacheFactory();

    // Render principale: workspace
    public String renderModel(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append("{\n");
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(renderNamespace(namespace));
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderNamespace(C4Namespace namespace) {
        Mustache mustache = MF.compile("templates/namespace.mustache");

        List<Map<String, Object>> comps =
                namespace.getComponents().stream()
                        .map(c -> {
                            Map<String, Object> model = new HashMap<>();

                            model.put("kind", c.getKind().toLowerCase());
                            model.put("id", c.getId().replace(".", "-"));
                            model.put("name", c.getName());
                            model.put("technology", c.getKind());
                            model.put("description", c.getDescription());

                            //model.put("icon", iconFor(c.getResource().getKind()));

                            // 🔹 labels
                            model.put(
                                    "labels",
                                    Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                                            .map(e -> Map.of(
                                                    "key", e.getKey(),
                                                    "value", e.getValue()
                                            ))
                                            .toList()
                            );

                            // 🔹 annotations
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


        List<String> relations= new ArrayList<>();

        for (C4Relationship rel: namespace.getRelationships()){
            relations.add(rel.getSource()+" -> "+rel.getTarget());
        }

        Map<String, Object> ctx = Map.of(
                "name", namespace.getName(),
                "components", comps,
                "relations", relations
        );

        StringWriter writer = new StringWriter();
        mustache.execute(writer, ctx);
        return writer.toString();
    }

    private String iconFor(String kind) {
        return switch (kind) {
            case "Deployment" -> "kubernetes/deployment";
            case "StatefulSet" -> "kubernetes/statefulset";
            case "DaemonSet" -> "kubernetes/daemonset";
            case "Job" -> "kubernetes/job";
            default -> "default";
        };
    }

    // Render principale: workspace
    public String renderRelations(C4Model model) {
        StringBuilder sb = new StringBuilder();
        for (C4Relationship rel: model.getRelationships()){
            sb.append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    public String renderSpec(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("specification ").append("{").append("\n");
        for (String elementName: model.getSpecifications()){
            sb.append("element ").append(" ").append(elementName).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

}

