package model;

import io.fabric8.kubernetes.api.model.HasMetadata;
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
    private String id;
    private String image;
    private String kind;
    private String description="";
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, String> env = new HashMap<>();
    private HasMetadata resource;
    public C4Component(HasMetadata resource,String namespace , String name, String kind) {
        if(namespace!=null ){
            this.namespace=namespace;
        }
        this.resource=resource;
        this.id= kind.toLowerCase() + "_" + name;
        this.name = name;
        this.kind = kind;
    }

    public Map<String,String> getLabels(){
       return this.resource.getMetadata().getLabels();
    }

    public Map<String,String> getAnnotations(){
        return this.resource.getMetadata().getAnnotations();
    }

}