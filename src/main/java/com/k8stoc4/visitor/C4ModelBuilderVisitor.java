package com.k8stoc4.visitor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.k8stoc4.model.*;

import java.util.*;

import static com.k8stoc4.visitor.VisitorUtils.containerMatchesSelector;

@Slf4j
@Getter
@Setter
public class C4ModelBuilderVisitor implements KubernetesResourceVisitor {

    private final C4Model model = new C4Model();

    private C4Namespace getOrCreateSystem(String ns) {
        return model.getNamespaces().computeIfAbsent(ns, C4Namespace::new);
    }

    public void addServiceRelationships() {
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
            for (C4Component component : namespace.getComponents()) {
                if (component.getKind().equalsIgnoreCase("service")) {
                    Map<String, String> selector = ((Service)component.getResource()).getSpec().getSelector();
                    
                    if (selector != null && !selector.isEmpty()) {
                        for (C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                C4Relationship rel = new C4Relationship(
                                    component.getNamespace() + "." + component.getId(),
                                    targetComp.getNamespace() + "." + targetComp.getId(),
                                    Constants.ROUTES_TO_RELATIONSHIP,
                                    Constants.TECHNOLOGY_TCP_HTTP
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(StatefulSet statefulSet) {
        model.getSpecifications().add(statefulSet.getKind().toLowerCase());
        String ns = Optional.ofNullable(statefulSet.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                statefulSet,
                statefulSet.getMetadata().getNamespace(),
                statefulSet.getMetadata().getName(),
                statefulSet.getKind());
        
        processPodSpec(namespace, component, statefulSet.getSpec().getTemplate().getSpec(),
                statefulSet.getSpec().getTemplate().getMetadata().getLabels());
        
        namespace.addComponents(component);
    }

    @Override
    public void visit(Deployment deployment) {
        model.getSpecifications().add(deployment.getKind().toLowerCase());
        String ns = Optional.ofNullable(deployment.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                deployment,
                deployment.getMetadata().getNamespace(),
                deployment.getMetadata().getName(),
                deployment.getKind());
        
        processPodSpec(namespace, component, deployment.getSpec().getTemplate().getSpec(),
                deployment.getSpec().getTemplate().getMetadata().getLabels());
        
        namespace.addComponents(component);
    }

    private void processPodSpec(C4Namespace namespace, C4Component component, PodSpec podSpec, Map<String, String> labels) {
        if (podSpec != null && podSpec.getContainers() != null) {
            Container c = podSpec.getContainers().get(0);
            component.setImage(c.getImage());
            component.setMetadata(labels);

            if (c.getEnvFrom() != null) {
                for (EnvFromSource envFrom : c.getEnvFrom()) {
                    addValueFromRelationship(namespace, component, envFrom);
                }
            }
            if (c.getEnv() != null) {
                for (EnvVar e : c.getEnv()) {
                    if (e.getValueFrom() != null) {
                        addValueFromKeyRelationship(namespace, component, e.getValueFrom());
                    } else {
                        component.getEnv().put(e.getName(), e.getValue());
                    }
                }
            }
        }

        if (podSpec != null && podSpec.getVolumes() != null) {
            for (Volume volume : podSpec.getVolumes()) {
                addVolumeRelationship(namespace, component, volume);
            }
        }
    }

    private void addVolumeRelationship(C4Namespace namespace, C4Component component, Volume volume) {
        String source = component.getNamespace() + "." + component.getId();
        String target = "";
        
        if (volume.getPersistentVolumeClaim() != null && volume.getPersistentVolumeClaim().getClaimName() != null) {
            target = component.getNamespace() + ".persistentvolumeclaim_" + volume.getPersistentVolumeClaim().getClaimName();
        }
        if (volume.getProjected() != null) {
            for (VolumeProjection projection : volume.getProjected().getSources()) {
                if (projection.getConfigMap() != null) {
                    target = component.getNamespace() + ".configmap_" + projection.getConfigMap().getName();
                } else if (projection.getSecret() != null) {
                    target = component.getNamespace() + ".secret_" + projection.getSecret().getName();
                }
            }
        }
        if (volume.getConfigMap() != null) {
            target = component.getNamespace() + ".configmap_" + volume.getConfigMap().getName();
        }
        if (volume.getSecret() != null) {
            target = component.getNamespace() + ".secret_" + volume.getSecret().getSecretName();
        }
        
        if (!target.isEmpty()) {
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.VOLUME_TECHNOLOGY));
        }
    }

    private void addValueFromRelationship(C4Namespace namespace, C4Component component, EnvFromSource valueFrom) {
        String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapRef() != null) {
            String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretRef() != null) {
            String target = component.getNamespace() + ".secret_" + valueFrom.getSecretRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    private void addValueFromKeyRelationship(C4Namespace namespace, C4Component component, EnvVarSource valueFrom) {
        String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapKeyRef() != null) {
            String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretKeyRef() != null) {
            String target = component.getNamespace() + ".secret_" + valueFrom.getSecretKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    @Override
    public void visit(Service svc) {
        model.getSpecifications().add(svc.getKind().toLowerCase());
        String ns = Optional.ofNullable(svc.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component service = new C4Component(svc, svc.getMetadata().getNamespace(),
                svc.getMetadata().getName(), svc.getKind());
        namespace.addComponents(service);
    }

    @Override
    public void visit(Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress = new C4Component(ing, ing.getMetadata().getNamespace(),
                ing.getMetadata().getName(), ing.getKind());
        namespace.addComponents(ingress);

        for (IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getService().getName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace()+"."+ingress.getId(),
                        ingress.getNamespace()+".service_"+svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    @Override
    public void visit(io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress = new C4Component(ing, ing.getMetadata().getNamespace(),
                ing.getMetadata().getName(), ing.getKind());
        namespace.addComponents(ingress);

        for (io.fabric8.kubernetes.api.model.networking.v1beta1.IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getServiceName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace() + "." + ingress.getId(),
                        ingress.getNamespace() + ".service_" + svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    public void visit(io.fabric8.kubernetes.api.model.extensions.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress = new C4Component(ing, ing.getMetadata().getNamespace(),
                ing.getMetadata().getName(), ing.getKind());
        ingress.setDescription(ing.getSpec().getRules().get(0).getHost());
        namespace.addComponents(ingress);

        for (io.fabric8.kubernetes.api.model.extensions.IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getServiceName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace() + "." + ingress.getId(),
                        ingress.getNamespace() + ".service_" + svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    @Override
    public void visit(HasMetadata resource) {
        model.getSpecifications().add(resource.getKind().toLowerCase());
        String ns = Optional.ofNullable(resource.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace system = getOrCreateSystem(ns);
        C4Component component = new C4Component(resource, resource.getMetadata().getNamespace(), resource.getMetadata().getName(), resource.getKind());
        system.addComponents(component);
    }

}
