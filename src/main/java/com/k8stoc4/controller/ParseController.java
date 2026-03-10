package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ParseController {

    private final String input;
    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;

    public ParseController(String input, Optional<String> defaultNamespace, Optional<String> groupByLabel) {
        this.input = input;
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
    }

    public C4DslRenderer.Output execute() {
        try (KubernetesClient client = new KubernetesClientBuilder().build();
             FileInputStream fis = new FileInputStream(input)) {
            String s = new String(fis.readAllBytes());
            List<HasMetadata> resources = client.load(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))).items();
            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor(defaultNamespace);
            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }
            visitor.addAllRelationships();
            groupByLabel.ifPresent(visitor::groupComponentsByLabel);
            C4DslRenderer renderer=new C4DslRenderer();
            return renderer.render(visitor.getModel());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Input file not found: " + input, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading input file: " + input, e);
        }
    }
}
