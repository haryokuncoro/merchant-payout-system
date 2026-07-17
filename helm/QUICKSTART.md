# Quick Start Guide for Helm Chart

## Local Development Setup (Minikube)

### 1. Start Minikube
```bash
minikube start --cpus=4 --memory=8192
eval $(minikube docker-env)
```

### 2. Build and load your image
```bash
# From project root
./gradlew build
docker build -t merchant-payout-system:latest .
```

### 3. Install the chart
```bash
helm install merchant-payout ./helm \
  -n merchant-payout \
  --create-namespace \
  -f helm/examples/values-development.yaml
```

### 4. Wait for all services to be ready
```bash
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=merchant-payout-system \
  -n merchant-payout --timeout=300s
```

### 5. Access the application
```bash
minikube service merchant-payout-system -n merchant-payout
# or
kubectl port-forward -n merchant-payout svc/merchant-payout-system 8080:8080
```

## Production Setup (AWS EKS)

### 1. Create ECR repository and push image
```bash
aws ecr create-repository --repository-name merchant-payout-system
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
docker tag merchant-payout-system:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/merchant-payout-system:1.0.0
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/merchant-payout-system:1.0.0
```

### 2. Prepare secrets
```bash
# Create a secrets file with your actual values
cat > prod-secrets.yaml << EOF
app:
  secrets:
    JWT_SECRET: "your-secret"
    STRIPE_API_KEY_SG: "sk_live_xxx"
    STRIPE_API_KEY_HK: "sk_live_xxx"
    STRIPE_API_KEY_MY: "sk_live_xxx"
    STRIPE_WEBHOOK_SECRET: "whsec_xxx"
EOF
```

### 3. Create namespace and secrets
```bash
kubectl create namespace merchant-payout
kubectl create secret generic stripe-secrets \
  --from-literal=api-key-sg=$STRIPE_API_KEY_SG \
  --from-literal=api-key-hk=$STRIPE_API_KEY_HK \
  --from-literal=api-key-my=$STRIPE_API_KEY_MY \
  --from-literal=webhook-secret=$STRIPE_WEBHOOK_SECRET \
  -n merchant-payout
```

### 4. Install the chart
```bash
helm install merchant-payout ./helm \
  -n merchant-payout \
  -f helm/examples/values-production.yaml \
  --set image.repository=<account-id>.dkr.ecr.us-east-1.amazonaws.com/merchant-payout-system \
  --set image.tag=1.0.0 \
  --set app.secrets.JWT_SECRET="$JWT_SECRET" \
  --set app.secrets.STRIPE_API_KEY_SG="$STRIPE_API_KEY_SG" \
  --set app.secrets.STRIPE_API_KEY_HK="$STRIPE_API_KEY_HK" \
  --set app.secrets.STRIPE_API_KEY_MY="$STRIPE_API_KEY_MY" \
  --set app.secrets.STRIPE_WEBHOOK_SECRET="$STRIPE_WEBHOOK_SECRET"
```

### 5. Set up ingress (optional)
```bash
# Install cert-manager if not present
helm repo add jetstack https://charts.jetstack.io
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager --create-namespace \
  --set installCRDs=true

# Install nginx-ingress
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install nginx-ingress ingress-nginx/ingress-nginx
```

## Useful Commands

### View logs
```bash
# All pods
kubectl logs -f -n merchant-payout -l app.kubernetes.io/name=merchant-payout-system

# Specific pod
kubectl logs -f -n merchant-payout <pod-name>

# Previous logs (if pod restarted)
kubectl logs -f -n merchant-payout <pod-name> --previous
```

### Scale deployment
```bash
kubectl scale deployment merchant-payout-system -n merchant-payout --replicas=3
```

### Update deployment
```bash
helm upgrade merchant-payout ./helm \
  -n merchant-payout \
  -f helm/examples/values-production.yaml \
  --set image.tag=1.0.1
```

### Check resource usage
```bash
kubectl top nodes
kubectl top pods -n merchant-payout
```

### Debug pod
```bash
kubectl exec -it -n merchant-payout <pod-name> -- /bin/bash
```

### View events
```bash
kubectl get events -n merchant-payout --sort-by='.lastTimestamp'
```

## Chart Values Reference

Key configurations you might want to override:

```yaml
# Number of application replicas
app:
  replicaCount: 3

# Resource limits
app:
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
    limits:
      memory: "2Gi"
      cpu: "1000m"

# Database size
postgresql:
  primary:
    persistence:
      size: 100Gi

# Kafka replication (for HA)
kafka:
  replicaCount: 3

# Enable autoscaling
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10

# Configure ingress
ingress:
  enabled: true
  hosts:
    - host: payout-system.example.com
      paths:
        - path: /
          pathType: Prefix
```

## Troubleshooting

### Pods not starting
```bash
kubectl describe pod <pod-name> -n merchant-payout
kubectl logs <pod-name> -n merchant-payout
```

### Database connection issues
```bash
kubectl exec -it -n merchant-payout deployment/postgresql -- \
  psql -U postgres -d payout_system -c "SELECT 1;"
```

### Kafka connection issues
```bash
kubectl exec -it -n merchant-payout deployment/kafka -- \
  kafka-broker-api-versions --bootstrap-server kafka:9092
```

### PVC issues
```bash
kubectl get pvc -n merchant-payout
kubectl describe pvc <pvc-name> -n merchant-payout
```

## Rollback
```bash
helm rollback merchant-payout 1 -n merchant-payout
```

## Uninstall
```bash
helm uninstall merchant-payout -n merchant-payout
kubectl delete namespace merchant-payout
```

