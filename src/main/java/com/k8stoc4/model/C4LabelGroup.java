package com.k8stoc4.model;

import com.k8stoc4.presenter.PresenterUtils;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@ToString
public class C4LabelGroup {
    private final String name;
    private final String labelKey;
    private final String labelValue;
    private final Set<C4Component> components = new LinkedHashSet<>();
    private final Set<C4Relationship> relationships = new LinkedHashSet<>();

    public C4LabelGroup(final String name, final String labelKey, final String labelValue) {
        this.name = PresenterUtils.sanitizeComponentId(name);
        this.labelKey = labelKey;
        this.labelValue = labelValue;
    }

    public void addComponents(final C4Component c) { components.add(c); }
    public void addRelationship(final C4Relationship r) { relationships.add(r); }
}
