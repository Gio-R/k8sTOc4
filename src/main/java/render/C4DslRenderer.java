package render;

import model.*;

public class C4DslRenderer {

    private static final String INDENT = "  ";

    // Render principale: workspace
    public String renderModel(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append("{\n");
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(renderNamespace(namespace, 1));
        }
        sb.append("}\n");
        return sb.toString();
    }

    // Render di un sistema
    private String renderNamespace(C4Namespace namespace, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);
        sb.append(indent).append("namespace ").append(namespace.getName()).append(" {\n");

        for (C4Component container : namespace.getComponents()) {
            sb.append(renderComponent(container, level + 1));
        }

        sb.append(renderRelations(namespace));
        sb.append(indent).append("}\n");
        return sb.toString();
    }

    // Render di un container
    private String renderContainer(C4Container container, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);

        sb.append(indent).append("container ").append(container.getName()).append(" {\n");
        sb.append(indent).append(INDENT)
                .append("technology \"").append(container.getType()).append("\"\n");

        // Aggiunge metadata come description se presente
        if (!container.getMetadata().isEmpty()) {
            for (var entry : container.getMetadata().entrySet()) {
                sb.append(indent).append(INDENT)
                        .append(entry.getKey()).append(" \"").append(entry.getValue()).append("\"\n");
            }
        }

        // Aggiunge componenti
        for (C4Component comp : container.getComponents()) {
            sb.append(renderComponent(comp, level + 1));
        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }

    // Render di un componente
    private String renderComponent(C4Component component, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);

        //La spec deve usare i nomi degli oggetti kubernetes
        sb.append(indent).append(component.getKind().toLowerCase()).append(" ")
                .append(component.getName())
                .append(" {\n");

        sb.append(indent)
                .append(INDENT)
                .append("technology \"")
                .append(component.getKind())
                .append("\"\n");

        sb.append(indent)
                .append(INDENT)
                .append("description \"")
                .append(component.getKind())
                .append("\"\n");

//        if (!component.getEnv().isEmpty()) {
//            for (var entry : component.getEnv().entrySet()) {
//                sb.append(indent).append(INDENT)
//                        .append(entry.getKey()).append(" \"").append(entry.getValue()).append("\"\n");
//            }
//        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }

    // Render principale: workspace
    public String renderRelations(C4Model model) {
        StringBuilder sb = new StringBuilder();
        for (C4Relationship rel: model.getRelationships()){
            sb.append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    public String renderRelations(C4Namespace namespace) {
        StringBuilder sb = new StringBuilder();

        for (C4Relationship rel: namespace.getRelationships()){
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

