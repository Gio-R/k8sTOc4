package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
public class C4Namespace {
    private String name;
    private Set<C4Component> components = new HashSet<>();
    private Set<C4Relationship> relationships = new HashSet<>();

    public C4Namespace(String name) { this.name = name; }
    public void addCompoments(C4Component c) { components.add(c); }
    public void addRelationship(C4Relationship r) { relationships.add(r); }
}