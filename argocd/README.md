# ArgoCD Setup for Merchant Payout System

This directory contains all ArgoCD configurations for managing Kubernetes deployments of the Merchant Payout System across multiple environments.

## Directory Structure

```
argocd/
├── namespace.yaml                 # ArgoCD namespace
├── bootstrap/                     # Bootstrap ArgoCD installation
│   └── argocd-app.yaml           # ArgoCD Helm chart Application
├── applications/                  # ArgoCD Applications for each environment
│   ├── app-dev.yaml              # Development environment
│   ├── app-staging.yaml          # Staging environment
│   └── app-prod.yaml             # Production environment
├── applicationset/               # ApplicationSets for multi-environment deployments
│   └── app-environments.yaml     # Multi-environment ApplicationSet
├── projects/                     # ArgoCD AppProjects
│   └── projects.yaml             # Project definitions and RBAC
├── repositories/                 # Git and Helm repositories
│   └── repositories.yaml         # Repository credentials
├── secrets/                      # Kubernetes Secrets
│   ├── secrets-dev.yaml          # Development secrets (safe to commit)
│   └── secrets-staging-prod.yaml # Staging/Production secrets template
├── config/                       # ArgoCD configuration
│   └── argocd-cm.yaml            # ArgoCD ConfigMap
└── README.md                     # This file
```

## Prerequisites

- Kubernetes 1.20+ cluster
- Helm 3.0+
- ArgoCD 2.5.0+
- kubectl configured to access your cluster
- GitHub repository access

## Installation

### Step 1: Create ArgoCD Namespace

```bash
kubectl apply -f argocd/namespace.yaml
```

### Step 2: Install ArgoCD using Helm

```bash
# Add Argo Helm repository
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

# Install ArgoCD
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values argocd/bootstrap/argocd-app.yaml
```

Or use the bootstrapped Application (once ArgoCD is running):

```bash
kubectl apply -f argocd/bootstrap/argocd-app.yaml
```

### Step 3: Configure Repository Access

Update credentials in `argocd/repositories/repositories.yaml`:

```bash
# Edit the GitHub token and Docker registry credentials
kubectl apply -f argocd/repositories/repositories.yaml
```

### Step 4: Set Up Projects and RBAC

```bash
kubectl apply -f argocd/projects/projects.yaml
```

### Step 5: Create Environment-Specific Secrets

For development (safe to commit):
```bash
kubectl apply -f argocd/secrets/secrets-dev.yaml
```

For staging and production (use Sealed Secrets or External Secrets):
```bash
# See "Managing Secrets" section below
```

### Step 6: Deploy Applications

Choose your approach:

**Option A: Deploy individual applications**
```bash
kubectl apply -f argocd/applications/app-dev.yaml
kubectl apply -f argocd/applications/app-staging.yaml
kubectl apply -f argocd/applications/app-prod.yaml
```

**Option B: Use ApplicationSet (recommended for multi-environment)**
```bash
kubectl apply -f argocd/applicationset/app-environments.yaml
```

### Step 7: Access ArgoCD UI

```bash
# Port forward
kubectl port-forward -n argocd svc/argocd-server 8080:443

# Access at: https://localhost:8080

# Get initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Change admin password
argocd account update-password --account admin --new-password <new-password>
```

Or use an Ingress (configure in production):

```yaml
ingress:
  enabled: true
  ingressClassName: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: argocd.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: argocd-tls
      hosts:
        - argocd.example.com
```

## Folder Structure Explanation

### bootstrap/
Contains the ArgoCD installation manifest that deploys ArgoCD itself using Helm. This enables the "GitOps of GitOps" pattern.

### applications/
Contains individual ArgoCD Application resources:
- **app-dev.yaml**: Development environment with:
  - 1 replica for app
  - 1 Kafka broker
  - Stripe mock enabled
  - Auto-sync enabled
  
- **app-staging.yaml**: Staging environment with:
  - 2 replicas for app
  - 2 Kafka brokers
  - Real Stripe integration (test keys)
  - Auto-sync with pruning enabled
  
- **app-prod.yaml**: Production environment with:
  - 3 replicas for app
  - 3 Kafka brokers
  - Real Stripe integration
  - Production RBAC with restricted access
  - Pod anti-affinity rules

### applicationset/
ApplicationSets allow you to manage multiple Applications from a single manifest. The example includes:
- Multi-environment generator
- Dynamic value templating
- Environment-specific configurations

### projects/
Defines RBAC and access control:
- **default**: For dev and staging
- **production**: Restricted access to production resources

### repositories/
Manages credentials for:
- Git repositories (GitHub, GitLab, etc.)
- Helm repositories
- Docker registries

### secrets/
Kubernetes Secrets for sensitive configuration:
- **secrets-dev.yaml**: Safe to commit (test values)
- **secrets-staging-prod.yaml**: Template for real secrets (DO NOT COMMIT)

### config/
ArgoCD configuration:
- **argocd-cm.yaml**: ConfigMap with RBAC policies, notifications, and settings

## Managing Secrets Securely

### Option 1: Sealed Secrets (Recommended)

```bash
# Install Sealed Secrets controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.18.0/controller.yaml

# Seal your secret
echo -n 'my-secret' | kubectl create secret generic app-secret --dry-run=client --from-file=/dev/stdin -o yaml | kubeseal -f - > sealed-secret.yaml

# Apply sealed secret
kubectl apply -f sealed-secret.yaml
```

### Option 2: External Secrets Operator

```bash
# Install External Secrets
helm repo add external-secrets https://external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets-system --create-namespace

# Create ExternalSecret referencing AWS Secrets Manager, Vault, etc.
kubectl apply -f argocd/secrets/external-secret.yaml
```

### Option 3: AWS Secrets Manager / Azure Key Vault

```bash
# Use IRSA (IAM Roles for Service Accounts) to access AWS Secrets Manager
# Or Workload Identity for Azure Key Vault
```

## Updating Applications

### Manual Sync
```bash
argocd app sync merchant-payout-dev
argocd app sync merchant-payout-staging
argocd app sync merchant-payout-prod
```

### Automatic Sync
Applications are configured with automatic sync enabled:
- Automatically syncs when Git repository changes
- Auto-pruning removes resources deleted from Git

To disable auto-sync:
```bash
argocd app set merchant-payout-dev --sync-policy none
```

## Rollback

### Rollback to Previous Version
```bash
# View sync history
argocd app history merchant-payout-prod

# Rollback to previous deployment
argocd app rollback merchant-payout-prod 1
```

## GitOps Workflow

### 1. Develop
```bash
git checkout -b feature/add-new-endpoint
# Make changes to helm/values.yaml
```

### 2. Push to GitHub
```bash
git add .
git commit -m "Add new feature"
git push origin feature/add-new-endpoint
```

### 3. Create Pull Request
- Create PR to `develop` branch
- CI/CD runs tests
- PR is reviewed

### 4. Merge to Develop
```bash
# PR is merged to develop branch
# ArgoCD automatically deploys to merchant-payout-dev
```

### 5. Release to Staging
```bash
git checkout main
git merge develop
git push origin main
# ArgoCD automatically deploys to merchant-payout-staging
```

### 6. Release to Production
```bash
git tag v1.0.0
git push origin v1.0.0
# Create release from tag
# ArgoCD deploys to merchant-payout-prod when tag is updated
```

## Monitoring and Observability

### View Application Status
```bash
# Get all applications
argocd app list

# Get application details
argocd app get merchant-payout-dev
argocd app get merchant-payout-staging
argocd app get merchant-payout-prod

# Watch application status
argocd app wait merchant-payout-prod
```

### View Application Logs
```bash
# ArgoCD server logs
kubectl logs -f -n argocd deployment/argocd-server

# ArgoCD application controller logs
kubectl logs -f -n argocd deployment/argocd-application-controller

# Repository server logs
kubectl logs -f -n argocd deployment/argocd-repo-server
```

### Metrics and Dashboards
```bash
# Port forward Prometheus (if installed)
kubectl port-forward -n argocd svc/prometheus 9090:9090

# Access Grafana dashboards
kubectl port-forward -n monitoring svc/grafana 3000:3000
```

## Webhook Integration

### GitHub Webhook

1. Get ArgoCD webhook URL:
```bash
argocd admin dashboard generate-auth --insecure
```

2. Configure in GitHub:
   - Settings → Webhooks → Add webhook
   - Payload URL: `https://argocd.example.com/api/webhook`
   - Content type: `application/json`
   - Events: Push events

### Continuous Deployment Trigger
```bash
# ArgoCD polls repository every 3 minutes by default
# For immediate deployment on push, use webhooks
```

## Advanced Configuration

### Multi-Cluster Deployments
```bash
# Register additional clusters
argocd cluster add <cluster-name>

# Update destination.server in Applications
destination:
  server: https://<cluster-ip>:6443
  namespace: merchant-payout-prod
```

### Notifications to Slack
```bash
# Install Argo Notifications controller
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj-labs/argocd-notifications/release-1.0/manifests/install.yaml

# Configure Slack integration
argocd admin notifications install
```

### Policy as Code with Kyverno
```bash
# Install Kyverno
helm repo add kyverno https://kyverno.github.io/kyverno/
helm install kyverno kyverno/kyverno --namespace kyverno --create-namespace

# Define policies in argocd/policies/kyverno-policies.yaml
```

## Troubleshooting

### Application Sync Fails
```bash
# Check application status
argocd app get merchant-payout-dev

# View sync logs
argocd app logs merchant-payout-dev --kind Application

# Describe pod for more details
kubectl describe pod -n merchant-payout-dev
```

### Repository Access Issues
```bash
# Test repository connection
argocd repo list
argocd repo add https://github.com/your-org/merchant-payout-system --username <user> --password <token>
```

### Webhook Not Triggering
```bash
# Check webhook deliveries in GitHub repository settings
# Verify ArgoCD is accessible from GitHub
# Check application webhook configuration
argocd app get merchant-payout-dev
```

## Best Practices

1. **GitOps Principles**
   - All configuration in Git
   - Single source of truth
   - Declarative deployments
   - Automated rollbacks

2. **Security**
   - Use Sealed Secrets or External Secrets
   - Limit RBAC access by environment
   - Use separate Git branches per environment
   - Audit all deployments

3. **High Availability**
   - Deploy ArgoCD in HA mode
   - Use persistent storage for Redis
   - Configure multiple replicas

4. **Monitoring**
   - Set up alerts for sync failures
   - Monitor application health
   - Track deployment metrics

5. **Documentation**
   - Document environment-specific configurations
   - Keep deployment runbooks
   - Document incident procedures

## Support and Resources

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [ArgoCD Getting Started](https://argo-cd.readthedocs.io/en/stable/getting_started/)
- [ArgoCD Best Practices](https://argo-cd.readthedocs.io/en/stable/user-guide/best_practices/)
- [GitOps Best Practices](https://www.weave.works/technologies/gitops/)

## Cleanup

### Delete an Application
```bash
argocd app delete merchant-payout-dev
```

### Uninstall ArgoCD
```bash
helm uninstall argocd -n argocd
kubectl delete namespace argocd
```

### Remove Sealed Secrets
```bash
kubectl delete namespace sealed-secrets
```

