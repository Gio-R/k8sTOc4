package model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class C4Component {

    private String namespace="default";
    private String name;
    private String image;
    private String kind;
    private String description;
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, String> env = new HashMap<>();
    public C4Component(String namespace , String name, String kind) {
        if(namespace!=null ){
            this.namespace=namespace;
        }
        this.name = name;
        this.kind = kind;
    }

}