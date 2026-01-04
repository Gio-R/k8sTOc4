package com.k8stoc4.visitor;

public interface K8sVisitable {

    void accept(KubernetesResourceVisitor visitor);
}
