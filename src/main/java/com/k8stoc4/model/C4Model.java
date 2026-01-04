package com.k8stoc4.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@ToString
public  class C4Model {
    private final Map<String, C4Namespace> namespaces = new HashMap<>();
    private Set<String> specifications = new HashSet<>();
    private Set<C4Relationship> relationships = new HashSet<>();

    public C4Model(){
        specifications.add("namespace");
    }
    public void addNamespace( C4Namespace s) { namespaces.put(s.getName(),s);}

    public void addRelationship(C4Relationship r) { relationships.add(r); }
 }
