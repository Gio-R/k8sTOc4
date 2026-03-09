package com.k8stoc4.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class C4LabelGroup {
    private String name;
    private String labelKey;
    private String labelValue;
    private Set<C4Component> components = new LinkedHashSet<>();
    private Set<C4Relationship> relationships = new LinkedHashSet<>();

    public C4LabelGroup(String name, String labelKey, String labelValue) {
        this.name = name;
        this.labelKey = labelKey;
        this.labelValue = labelValue;
    }

    public void addComponents(C4Component c) { components.add(c); }
    public void addRelationship(C4Relationship r) { relationships.add(r); }
}
