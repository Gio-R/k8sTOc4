package org.example;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.java.Log;
import model.C4Component;
import model.C4Namespace;
import model.C4Relationship;
import render.C4DslRenderer;
import visitor.C4ModelBuilderVisitor;
import visitor.VisitorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import static visitor.VisitorUtils.containerMatchesSelector;

@Log
public class KubernetesC4FromYamlVisitor {

    public static void main(String[] args) throws Exception {

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            FileInputStream fis=new FileInputStream(new File("src/main/resources/output-mida.yaml"));

            List<HasMetadata> resources = client.load(fis).items();
            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor();
            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }

            //Add service Relations
           for( String ns: visitor.getModel().getNamespaces().keySet() ){
               for(C4Component component:visitor.getModel().getNamespaces().get(ns).getComponents()){
                   if (component.getKind().equalsIgnoreCase("service")){

                       Map<String, String> selector = ((Service)component.getResource()).getSpec().getSelector();
                       if (selector != null && !selector.isEmpty()) {
                           for (C4Component targetsComp : visitor.getModel().getNamespaces().get(ns).getComponents()) {
                               if (containerMatchesSelector(targetsComp, selector)) {
                                   visitor.getModel().getNamespaces().get(ns).addRelationship(new C4Relationship(
                                           //TODO Refactor
                                           component.getNamespace()+".service_"+component.getName(),
                                           targetsComp.getNamespace()+"."+targetsComp.getId(),
                                           "routes to",
                                           "TCP/HTTP"
                                   ));
                               }
                           }
                       }

                   }

               }
           }


            C4DslRenderer renderer=new C4DslRenderer();
            //System.out.println(renderer.renderSpec(visitor.getModel()));
            //System.out.println(renderer.renderModel(visitor.getModel()));

            try (FileWriter f = new FileWriter("spec.c4", false)) {
                f.write(renderer.renderSpec(visitor.getModel()));
            }
            try (FileWriter f = new FileWriter("model.c4", false)) {
                f.write(renderer.renderModel(visitor.getModel()));
            }

        }
    }
}
