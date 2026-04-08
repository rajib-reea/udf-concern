# UDF Microservice Deployment Guide

## Overview

This guide covers the deployment of the UDF Microservice in various environments, from local development to production Kubernetes clusters.

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Memory**: Minimum 2GB RAM, recommended 4GB+
- **Disk**: 500MB for application, plus database storage
- **Network**: Access to database and message broker

### External Dependencies
- **Database**: PostgreSQL 12+ or MySQL 8+
- **Message Broker**: Kafka 2.8+ or RabbitMQ 3.8+
- **Cache**: Redis 6+ (optional, for performance)

## Local Development

### Using Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd udf-microservice
   ```

2. **Start dependencies**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

3. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with local settings
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Verify deployment**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Manual Setup

1. **Install dependencies**
   ```bash
   # PostgreSQL
   brew install postgresql  # macOS
   sudo apt install postgresql postgresql-contrib  # Ubuntu

   # Kafka
   brew install kafka  # macOS
   # Or download from kafka.apache.org
   ```

2. **Create database**
   ```sql
   CREATE DATABASE udf_db;
   CREATE USER udf_user WITH PASSWORD 'udf_password';
   GRANT ALL PRIVILEGES ON DATABASE udf_db TO udf_user;
   ```

3. **Configure application.yml**
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/udf_db
       username: udf_user
       password: udf_password

     kafka:
       bootstrap-servers: localhost:9092
   ```

## Containerization

### Docker Image Build

1. **Build the image**
   ```bash
   docker build -t udf-microservice:latest .
   ```

2. **Run with dependencies**
   ```bash
   docker-compose up
   ```

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  udf-microservice:
    image: udf-microservice:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - kafka
    networks:
      - udf-network

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=udf_db
      - POSTGRES_USER=udf_user
      - POSTGRES_PASSWORD=udf_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - udf-network

  kafka:
    image: confluentinc/cp-kafka:7.3.0
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    depends_on:
      - zookeeper
    networks:
      - udf-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.3.0
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
    networks:
      - udf-network

volumes:
  postgres_data:

networks:
  udf-network:
    driver: bridge
```

## Kubernetes Deployment

### Namespace Setup

```bash
kubectl create namespace udf-system
```

### ConfigMaps and Secrets

1. **Application Configuration**
   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: udf-config
     namespace: udf-system
   data:
     SPRING_PROFILES_ACTIVE: "k8s"
     SPRING_DATASOURCE_URL: "jdbc:postgresql://udf-postgres:5432/udf_db"
     SPRING_KAFKA_BOOTSTRAP_SERVERS: "udf-kafka:9092"
   ```

2. **Database Secret**
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
     name: udf-db-secret
     namespace: udf-system
   type: Opaque
   data:
     username: dWRmX3VzZXI=  # base64 encoded
     password: dWRmX3Bhc3N3b3Jk  # base64 encoded
   ```

### Database Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udf-postgres
  namespace: udf-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: udf-postgres
  template:
    metadata:
      labels:
        app: udf-postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: "udf_db"
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: udf-db-secret
              key: username
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: udf-db-secret
              key: password
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: udf-postgres-pvc
```

### Kafka Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udf-kafka
  namespace: udf-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: udf-kafka
  template:
    metadata:
      labels:
        app: udf-kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.3.0
        ports:
        - containerPort: 9092
        env:
        - name: KAFKA_BROKER_ID
          value: "1"
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "udf-zookeeper:2181"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://udf-kafka:9092"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
```

### Application Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udf-microservice
  namespace: udf-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: udf-microservice
  template:
    metadata:
      labels:
        app: udf-microservice
    spec:
      containers:
      - name: udf-microservice
        image: your-registry/udf-microservice:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: udf-config
        - secretRef:
            name: udf-db-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### Service Definition

```yaml
apiVersion: v1
kind: Service
metadata:
  name: udf-microservice
  namespace: udf-system
spec:
  selector:
    app: udf-microservice
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### Ingress Configuration

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: udf-ingress
  namespace: udf-system
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: udf.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: udf-microservice
            port:
              number: 80
```

## Configuration Management

### Environment-Specific Configurations

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  kafka:
    enabled: false

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://udf-postgres:5432/udf_db
  kafka:
    bootstrap-servers: udf-kafka:9092
```

### External Configuration with Config Server

```yaml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      name: udf-microservice
```

## Monitoring and Observability

### Health Checks

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  health:
    probes:
      enabled: true
```

### Metrics Collection

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

### Logging

```yaml
logging:
  level:
    com.yourcompany.udf: INFO
    org.springframework: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Backup and Recovery

### Database Backup

```bash
# PostgreSQL backup
kubectl exec -n udf-system udf-postgres-0 -- pg_dump -U udf_user udf_db > udf_backup.sql

# Automated backup cron job
kubectl apply -f backup-cronjob.yml
```

### Application Logs

```bash
# View logs
kubectl logs -n udf-system deployment/udf-microservice

# Log aggregation with Fluentd/ELK stack
```

## Scaling

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: udf-hpa
  namespace: udf-system
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: udf-microservice
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Database Scaling

- **Read Replicas**: For read-heavy workloads
- **Connection Pooling**: PgBouncer for PostgreSQL
- **Sharding**: For very large datasets

## Security

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: udf-network-policy
  namespace: udf-system
spec:
  podSelector:
    matchLabels:
      app: udf-microservice
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
```

### TLS Configuration

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: udf-tls
  namespace: udf-system
spec:
  secretName: udf-tls-secret
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - udf.yourdomain.com
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check database connectivity
   kubectl exec -it udf-postgres-0 -n udf-system -- psql -U udf_user -d udf_db
   ```

2. **Pod Startup Failures**
   ```bash
   # Check pod events
   kubectl describe pod <pod-name> -n udf-system
   ```

3. **Performance Issues**
   ```bash
   # Check resource usage
   kubectl top pods -n udf-system
   ```

### Debug Commands

```bash
# Port forward for local testing
kubectl port-forward -n udf-system svc/udf-microservice 8080:80

# Execute into running container
kubectl exec -it <pod-name> -n udf-system -- /bin/bash

# View configuration
kubectl get configmap udf-config -n udf-system -o yaml
```