# Merchant Payout System Helm Chart

A complete Helm chart for deploying the Merchant Payout System on Kubernetes, including PostgreSQL, Apache Kafka (with KRaft), Stripe Mock, and the Spring Boot application.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- A container registry to push your application image

## Chart Components

### Services Included:
1. **PostgreSQL** - Database for the payout system
2. **Apache Kafka** - Message streaming with KRaft mode (no Zookeeper)
3. **Stripe Mock** - Mock Stripe API for testing
4. **Spring Boot Application** - Main merchant payout system application

## Installation

### 1. Create a namespace
```bash
kubectl create namespace merchant-payout
```

### 2. Prepare your values
Create a `custom-values.yaml` file with your configuration:

```yaml
image:
  repository: your-registry/merchant-payout-system
  tag: "1.0.0"

app:
  secrets:
    JWT_SECRET: "your-jwt-secret-key"
    STRIPE_API_KEY_SG: "sk_live_..."
    STRIPE_API_KEY_HK: "sk_live_..."
    STRIPE_API_KEY_MY: "sk_live_..."
    STRIPE_WEBHOOK_SECRET: "whsec_..."
```

### 3. Install the chart
```bash
helm install merchant-payout ./helm \
  -n merchant-payout \
  -f custom-values.yaml
```

### 4. Verify the deployment
```bash
kubectl get pods -n merchant-payout
kubectl get svc -n merchant-payout
```

## Configuration

### PostgreSQL
- **Enabled by default**: Set `postgresql.enabled: false` to disable
- **Storage**: Default 10Gi, configure with `postgresql.primary.persistence.size`
- **Database**: `payout_system` (configurable via `postgresql.auth.database`)

### Kafka
- **Enabled by default**: Set `kafka.enabled: false` to disable
- **KRaft Mode**: Uses built-in controller, no external Zookeeper required
- **Storage**: Default 10Gi, configure with `kafka.persistence.size`
- **Ports**: 
  - Internal: 9092 (broker), 9093 (controller)
  - External: 29092 (NodePort)

### Stripe Mock
- **Enabled by default**: Set `stripeMock.enabled: false` to disable
- **Port**: 12111

### Spring Boot Application
- **Replicas**: Default 1, configure with `app.replicaCount`
- **Image**: Configure with `image.repository` and `image.tag`
- **Environment Variables**: Update via `app.env` and `app.secrets`
- **Resources**: Configure CPU/memory limits via `app.resources`

## Environment Variables

### Application Configuration (ConfigMap)
```yaml
app:
  env:
    SPRING_PROFILES_ACTIVE: dev
    SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    STRIPE_SKIP_SIGNATURE_CHECK: "true"
    STRIPE_MOCK_ENABLED: "true"
    STRIPE_MOCK_BASE_URL: "http://stripe-mock:12111"
```

### Sensitive Configuration (Secrets)
```yaml
app:
  secrets:
    SPRING_DATASOURCE_URL: "jdbc:postgresql://postgresql:5432/payout_system"
    SPRING_DATASOURCE_USERNAME: postgres
    JWT_SECRET: "" # REQUIRED
    STRIPE_API_KEY_SG: "" # REQUIRED
    STRIPE_API_KEY_HK: "" # REQUIRED
    STRIPE_API_KEY_MY: "" # REQUIRED
    STRIPE_WEBHOOK_SECRET: "" # REQUIRED
```

## Storage Classes

The chart uses PersistentVolumeClaims for PostgreSQL and Kafka. By default, it uses the `standard` storage class. To use a different storage class:

```yaml
postgresql:
  primary:
    persistence:
      storageClassName: gp3

kafka:
  persistence:
    storageClassName: gp3
```

## Ingress Configuration

To enable ingress (external access to the application):

```yaml
ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: payout-system.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: payout-system-tls
      hosts:
        - payout-system.example.com
```

## Upgrade

```bash
helm upgrade merchant-payout ./helm \
  -n merchant-payout \
  -f custom-values.yaml
```

## Uninstall

```bash
helm uninstall merchant-payout -n merchant-payout
```

## Troubleshooting

### Check logs
```bash
# Application logs
kubectl logs -n merchant-payout deployment/merchant-payout-system

# Database logs
kubectl logs -n merchant-payout deployment/postgresql

# Kafka logs
kubectl logs -n merchant-payout deployment/kafka
```

### Debug a pod
```bash
kubectl describe pod <pod-name> -n merchant-payout
```

### Port forward to access services locally
```bash
# Database
kubectl port-forward -n merchant-payout svc/postgresql 5432:5432

# Kafka
kubectl port-forward -n merchant-payout svc/kafka 9092:9092

# Application
kubectl port-forward -n merchant-payout svc/merchant-payout-system 8080:8080
```

## Health Checks

All services include liveness and readiness probes:
- **PostgreSQL**: Uses pg_isready
- **Kafka**: Uses kafka-broker-api-versions
- **Stripe Mock**: Uses HTTP health check
- **Application**: Uses Spring Boot actuator endpoints

## Support

For issues or questions, please refer to the project documentation or contact the development team.

