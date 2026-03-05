Architecture overview for k8sToC4 CLI

- Core: Java 17 CLI application using Picocli for CLI parsing. 
- Kubernetes parsing: Fabric8 Kubernetes Client to read manifests.
- DSL: Mustache templates produce Structurizr DSL (.c4) files.
- Model: Java classes representing C4 model (C4Model, C4Namespace, C4Component, etc.).
- Rendering: C4DslRenderer outputs DSL; Visitor pattern builds the model (C4ModelBuilderVisitor).
