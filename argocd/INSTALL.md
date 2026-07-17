# ArgoCD Installation and Configuration Guide

## Overview

This guide provides step-by-step instructions for installing and configuring ArgoCD for the Merchant Payout System with support for multiple environments (dev, staging, production).

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Automated Installation](#automated-installation)
3. [Manual Installation](#manual-installation)
4. [Configuration](#configuration)
5. [Deployment](#deployment)
6. [Verification](#verification)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools

- **kubectl** 1.20+ - Kubernetes command-line tool
  ```bash
  # macOS
  brew install kubectl
  
  # Linux
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
  ```

- **Helm** 3.0+ - Kubernetes package manager
  ```bash
  # macOS
  brew install helm
  
  # Linux
  curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
  ```

- **argocd** CLI (optional but recommended)
  ```bash
  curl -sSL -o argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
  sudo install -m 555 argocd /usr/local/bin/argocd
  ```

### Cluster Requirements

- Kubernetes 1.20+ cluster with at least 2 CPU and 4GB RAM
- Network access to GitHub (or your Git provider)
- Persistent storage provisioner (for PostgreSQL and Kafka)

### Access Credentials

- **GitHub** personal access token or SSH key
- **Docker Registry** credentials (if using private registry)
- **Stripe API keys** (test/live)

## Automated Installation

The fastest way to get ArgoCD up and running is using the provided setup script.

### Quick Start (Recommended)

```bash
cd /path/to/merchant-payout-system

# Run the setup script
./argocd/setup.sh
```

The script will:
1. Check prerequisites (kubectl, helm, argocd)
2. Create ArgoCD namespace
3. Install ArgoCD using Helm
4. Configure repositories and credentials
5. Apply ArgoCD projects and RBAC
6. Deploy applications
7. Display access instructions

### Setup Script with Environment Variables

```bash
# Set custom values before running the script
export ARGOCD_SERVER_URL="https://argocd.example.com"
export GITHUB_REPO_URL="https://github.com/your-org/merchant-payout-system"
export GITHUB_USERNAME="your-username"
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxx"

./argocd/setup.sh
```

## Manual Installation

### Step 1: Create ArgoCD Namespace

```bash
kubectl create namespace argocd
kubectl label namespace argocd name=argocd
```

### Step 2: Add Helm Repository

```bash
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
```

### Step 3: Install ArgoCD via Helm

#### Option A: Simple Installation

```bash
helm install argocd argo/argo-cd \
  --namespace argocd \
  --set server.service.type=LoadBalancer \
  --set server.extraArgs="{--insecure}" \
  --wait
```

#### Option B: Custom Configuration

```bash
helm install argocd argo/argo-cd \
  --namespace argocd \
  --values argocd/bootstrap/argocd-values.yaml \
  --wait
```

### Step 4: Wait for ArgoCD to Be Ready

```bash
kubectl wait --for=condition=available --timeout=300s \
  deployment/argocd-server -n argocd
```

### Step 5: Get Initial Admin Password

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d
```

### Step 6: Access ArgoCD

```bash
# Port forward to access locally
kubectl port-forward -n argocd svc/argocd-server 8080:443 &

# Open browser
open https://localhost:8080

# Or for UI at different port
kubectl port-forward -n argocd svc/argocd-server 8080:80 &
open http://localhost:8080
```

## Configuration

### 1. Update Git Repository URLs

Edit the following files with your repository information:

**argocd/applications/app-dev.yaml**
```yaml
source:
  repoURL: https://github.com/YOUR-ORG/merchant-payout-system
  targetRevision: develop
```

**argocd/applications/app-staging.yaml**
```yaml
source:
  repoURL: https://github.com/YOUR-ORG/merchant-payout-system
  targetRevision: main
```

**argocd/applications/app-prod.yaml**
```yaml
source:
  repoURL: https://github.com/YOUR-ORG/merchant-payout-system
  targetRevision: v1.0.0
```

### 2. Configure Repository Credentials

Update `argocd/repositories/repositories.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: github-repo
  namespace: argocd
  labels:
    argocd.argoproj.io/secret-type: repository
stringData:
  type: git
  url: https://github.com/YOUR-ORG/merchant-payout-system
  username: YOUR-USERNAME
  password: YOUR-GITHUB-TOKEN  # or SSH key
```

Apply the configuration:

```bash
kubectl apply -f argocd/repositories/repositories.yaml
```

### 3. Configure RBAC (Optional)

Edit `argocd/config/argocd-cm.yaml` to set up user roles and permissions:

```yaml
rbac:
  default.policy.csv: |
    p, role:developers, applications, get, default/*, allow
    p, role:developers, applications, sync, default/*, allow
    p, role:staging-admins, applications, *, merchant-payout-staging/*, allow
    p, role:prod-admins, applications, *, merchant-payout-prod/*, allow
    
    g, your-github-team, role:developers
```

Apply the configuration:

```bash
kubectl apply -f argocd/config/argocd-cm.yaml
```

### 4. Configure Projects

Apply ArgoCD projects:

```bash
kubectl apply -f argocd/projects/projects.yaml
```

This creates:
- **default** project: For dev and staging environments
- **production** project: Restricted access to production environment

### 5. Set Up Secrets

#### Development (Safe to Commit)

```bash
kubectl apply -f argocd/secrets/secrets-dev.yaml
```

#### Staging and Production (Use Secret Management)

For production, DO NOT commit plain secrets. Use one of:

**Option 1: Sealed Secrets**
```bash
# Install Sealed Secrets
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.18.0/controller.yaml

# Seal your secrets
kubectl create secret generic app-secrets-prod \
  --from-literal=JWT_SECRET='your-secret' \
  --dry-run=client -o yaml | kubeseal -f - > sealed-secret.yaml

kubectl apply -f sealed-secret.yaml
```

**Option 2: External Secrets Operator**
```bash
# Install External Secrets
helm repo add external-secrets https://external-secrets.io
helm install external-secrets external-secrets/external-secrets \
  -n external-secrets-system --create-namespace

# Apply external secret configuration
kubectl apply -f argocd/secrets/external-secrets-example.yaml
```

**Option 3: AWS Secrets Manager / Azure Key Vault**
- Use Workload Identity or IAM roles for authentication
- See `argocd/secrets/external-secrets-example.yaml` for examples

## Deployment

### Deploy Individual Applications

```bash
# Deploy to development
kubectl apply -f argocd/applications/app-dev.yaml

# Deploy to staging
kubectl apply -f argocd/applications/app-staging.yaml

# Deploy to production
kubectl apply -f argocd/applications/app-prod.yaml
```

### Deploy Using ApplicationSet

For multi-environment deployments with a single manifest:

```bash
kubectl apply -f argocd/applicationset/app-environments.yaml
```

### Deploy All Using Kustomize

```bash
kubectl apply -k argocd/
```

## Verification

### Check ArgoCD Status

```bash
# Check pods
kubectl get pods -n argocd

# Check services
kubectl get svc -n argocd

# Check applications
kubectl get applications -n argocd

# View application details
argocd app list
argocd app get merchant-payout-dev
```

### Monitor Deployment

```bash
# Watch applications sync
kubectl get applications -n argocd -w

# View sync progress
argocd app get merchant-payout-dev --refresh

# Check sync status
argocd app wait merchant-payout-dev --sync
```

### Access Deployed Application

```bash
# Port forward to the application
kubectl port-forward -n merchant-payout-dev svc/merchant-payout-system 8080:8080

# Access the application
curl http://localhost:8080

# Check application health
curl http://localhost:8080/actuator/health
```

## Common Operations

### View Logs

```bash
# ArgoCD server logs
kubectl logs -f -n argocd deployment/argocd-server

# ArgoCD controller logs
kubectl logs -f -n argocd deployment/argocd-application-controller

# Application logs
kubectl logs -f -n merchant-payout-dev deployment/merchant-payout-system
```

### Trigger Manual Sync

```bash
# Sync specific application
argocd app sync merchant-payout-dev

# Sync with force flag
argocd app sync merchant-payout-dev --force

# Wait for sync to complete
argocd app wait merchant-payout-dev
```

### Rollback Deployment

```bash
# View history
argocd app history merchant-payout-dev

# Rollback to previous deployment
argocd app rollback merchant-payout-dev 1
```

### Update Application Configuration

```bash
# Edit application values
kubectl edit application merchant-payout-dev -n argocd

# Apply new Helm values
argocd app set merchant-payout-dev --helm-set image.tag=new-version
```

## Troubleshooting

### Application Sync Fails

```bash
# Get application status
argocd app get merchant-payout-dev

# Describe application
kubectl describe application merchant-payout-dev -n argocd

# Check application controller logs
kubectl logs -f -n argocd deployment/argocd-application-controller | grep merchant-payout-dev

# Check resource server logs
kubectl logs -f -n argocd deployment/argocd-repo-server
```

### Repository Authentication Issues

```bash
# Test repository connection
argocd repo list

# Re-add repository with correct credentials
argocd repo add <repo-url> \
  --username <username> \
  --password <password>

# Or update secret
kubectl patch secret github-repo -n argocd \
  -p '{"stringData":{"password":"NEW-TOKEN"}}'
```

### Pods Not Deploying

```bash
# Check if namespace exists
kubectl get namespace merchant-payout-dev

# Check pod status
kubectl get pods -n merchant-payout-dev
kubectl describe pod <pod-name> -n merchant-payout-dev

# Check events
kubectl get events -n merchant-payout-dev --sort-by='.lastTimestamp'

# Check PVC status
kubectl get pvc -n merchant-payout-dev
kubectl describe pvc <pvc-name> -n merchant-payout-dev
```

### Database Connection Issues

```bash
# Test database connection
kubectl exec -it -n merchant-payout-dev \
  deployment/merchant-payout-system -- \
  nc -zv postgresql 5432

# Check database logs
kubectl logs -f -n merchant-payout-dev deployment/postgresql
```

### Webhook Not Triggering

```bash
# Check GitHub webhook deliveries in repository settings
# Verify ArgoCD is accessible from GitHub

# Check webhook logs
kubectl logs -f -n argocd deployment/argocd-server | grep webhook

# Manually trigger sync
argocd app sync merchant-payout-dev --force
```

## Next Steps

1. **Configure GitOps Workflow**
   - Set up branching strategy (develop → main → releases)
   - Configure CI/CD pipeline
   - Set up PR reviews

2. **Set Up Notifications**
   - Configure Slack integration
   - Set up email notifications
   - Create PagerDuty alerts

3. **Monitor and Observability**
   - Install Prometheus and Grafana
   - Set up application monitoring
   - Configure log aggregation

4. **Security Hardening**
   - Enable GitHub OAuth
   - Set up RBAC policies
   - Configure network policies
   - Enable audit logging

5. **Disaster Recovery**
   - Document backup procedures
   - Test rollback procedures
   - Set up cross-cluster failover

## Support

For issues or questions:
- Check [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- Review [GitHub Issues](https://github.com/argoproj/argo-cd/issues)
- Consult [GitOps Guide](https://www.weave.works/technologies/gitops/)

## References

- [ArgoCD Official Documentation](https://argo-cd.readthedocs.io/)
- [Helm Chart Repository](https://github.com/argoproj/argo-helm)
- [GitOps Best Practices](https://www.weave.works/technologies/gitops/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

