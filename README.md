# k8sToC4 CLI

Tool CLI per convertire manifest Kubernetes in diagrammi C4 usando il DSL Structurizr.

## Descrizione

Questo tool trasforma risorse Kubernetes (Deployment, Service, Ingress, ConfigMap, Secret, ecc.) in diagrammi architetturali C4. Analizza i file YAML Kubernetes, crea un modello C4 rappresentativo e genera file `.c4` compatibili con gli strumenti di visualizzazione C4/Structurizr.

## Funzionalità

- **Parsing YAML Kubernetes**: Carica e interpreta manifest Kubernetes multipli
- **Pattern Visitor**: Architettura estensibile per aggiungere nuovi tipi di risorse
- **Mapping automatico**: Converte risorse K8s in componenti C4 con relazioni
- **Relazioni automatiche**:
  - Service → Pods (tramite selector)
  - Ingress → Service (HTTP routes)
  - Pods → ConfigMap (envFrom, env valueFrom)
  - Pods → Secret (envFrom, env valueFrom, volumes)
  - Pods → PersistentVolumeClaim (volumes)
- **Generazione DSL**: Output in formato Structurizr DSL (.c4)

## Risorse Kubernetes Supportate

- Deployment
- StatefulSet
- Service
- Ingress (v1, v1beta1, extensions)
- ConfigMap
- Secret
- PersistentVolumeClaim
- Altre risorse generiche (fallback)

## Prerequisiti

- Java 17 o superiore
- Maven 3.x

## Installazione

```bash
git clone <repository-url>
cd k8sTOc4
mvn -B -DskipTests=false package
```

## Utilizzo

### Comando base

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i <input-file.yaml>
```

### Opzioni

- `-i, --input`: File YAML Kubernetes di input (richiesto)
- `-o, --output`: Directory di output per i file .c4 (opzionale)

### Esempi

**Output su stdout**:
```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i src/main/resources/microservice.yaml
```

**Output su file**:
```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i src/main/resources/complex.yaml -o ./output
```

Questo genererà:
- `output/spec.c4`: Specificazione degli elementi C4
- `output/model.c4`: Modello C4 con namespace, componenti e relazioni

### Aiuto

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar --help
```

## Architettura

```
src/main/java/
└── com/k8stoc4/
    ├── cli/
    │   ├── Main.java                 # Entry point CLI
    │   └── commands/
    │       └── ParseCommand.java     # Comando parse
    ├── model/
    │   ├── C4Model.java              # Modello C4 principale
    │   ├── C4Namespace.java          # Rappresenta un namespace K8s
    │   ├── C4Component.java          # Rappresenta una risorsa K8s
    │   ├── C4Relationship.java       # Relazione tra componenti
    │   └── Constants.java            # Costanti utilizzate nel modello
    ├── visitor/
    │   ├── C4ModelBuilderVisitor.java # Visitor che costruisce il modello C4
    │   ├── KubernetesResourceVisitor.java # Interface visitor
    │   ├── K8sVisitable.java         # Adattatore per pattern Visitor
    │   └── VisitorUtils.java         # Utilità per matching selector
    └── render/
        └── C4DslRenderer.java        # Renderer DSL Structurizr
```

## Dipendenze

- **Picocli** 4.7.7: Framework CLI
- **Fabric8 Kubernetes Client** 7.4.0: Parsing risorse Kubernetes
- **Mustache** 0.9.10: Template engine per DSL
- **Lombok** 1.18.42: Riduzione boilerplate code
- **Logback** 1.5.20: Logging

## Esempio di Output

**spec.c4**:
```c4
specification {
  element deployment
  element service
  element ingress
}
```

**model.c4**:
```c4
namespace my-namespace {
  deployment my-deployment 'my-deployment' {
    technology "Deployment"
    description "My application deployment"
    metadata {
      labels '
        app: myapp
        version: v1
      '
      annotations '
      '
    }
  }
  service my-service 'my-service' {
    technology "Service"
    description "Load balancer for my app"
    metadata {
      labels '
      '
      annotations '
      '
    }
  }
  my-namespace.service_my-service -> my-namespace.my-deployment
}
```

## Come contribuire

1. Fork del repository
2. Creare branch feature (`git checkout -b feature/AmazingFeature`)
3. Commit delle modifiche (`git commit -m 'Add AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Aprire una Pull Request

## License

Questo progetto è distribuito sotto la Apache License 2.0 - vedi il file LICENSE per dettagli.
