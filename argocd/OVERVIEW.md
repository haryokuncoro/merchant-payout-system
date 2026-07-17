# Merchant Payout System - ArgoCD Complete Setup

## Overview

This directory contains a complete GitOps setup using ArgoCD to manage the deployment of the Merchant Payout System across multiple Kubernetes environments (development, staging, production).

## 📁 Directory Structure

```
argocd/
├── README.md                          # Comprehensive ArgoCD documentation
├── QUICKSTART.md                      # Quick start guide (5-minute setup)
├── INSTALL.md                         # Detailed installation guide
├── setup.sh                           # Automated setup script
├── kustomization.yaml                 # Kustomize configuration
├── namespace.yaml                     # ArgoCD namespace definition
│
├── bootstrap/                         # Bootstrap ArgoCD installation
│   └── argocd-app.yaml               # ArgoCD Helm chart as Application (GitOps of GitOps)
│
├── applications/                      # ArgoCD Applications for each environment
│   ├── app-dev.yaml                  # Development environment (1 replica, Stripe mock)
│   ├── app-staging.yaml              # Staging environment (2 replicas, test Stripe keys)
│   └── app-prod.yaml                 # Production environment (3 replicas, live config)
│
├── applicationset/                    # Multi-environment ApplicationSets
│   └── app-environments.yaml         # Single manifest for all environments
│
├── projects/                          # ArgoCD AppProjects (RBAC)
│   └── projects.yaml                 # Project definitions with access control
│
├── repositories/                      # Repository credentials
│   └── repositories.yaml             # Git and Docker registry credentials
│
├── secrets/                           # Kubernetes Secrets
│   ├── secrets-dev.yaml              # Development secrets (safe to commit)
│   ├── secrets-staging-prod.yaml     # Staging/Production template (DON'T COMMIT REAL VALUES)
│   └── external-secrets-example.yaml # External Secrets Operator examples
│
└── config/                            # ArgoCD configuration
    └── argocd-cm.yaml                # ArgoCD ConfigMap (RBAC, notifications, settings)
```

## 🚀 Quick Start

### Automated Setup (Recommended)

```bash
# Make script executable
chmod +x argocd/setup.sh

# Run setup script
./argocd/setup.sh

# Follow interactive prompts to configure your setup
```

### Manual Quick Start

```bash
# 1. Create namespace
kubectl create namespace argocd

# 2. Install ArgoCD via Helm
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
helm install argocd argo/argo-cd -n argocd

# 3. Port forward
kubectl port-forward -n argocd svc/argocd-server 8080:443

# 4. Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# 5. Login at https://localhost:8080

# 6. Add repository
argocd repo add https://github.com/YOUR-ORG/merchant-payout-system \
  --username YOUR-USERNAME \
  --password YOUR-TOKEN

# 7. Deploy application
kubectl apply -f argocd/applications/app-dev.yaml
```

## 📋 File Descriptions

### Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Complete ArgoCD documentation with advanced topics |
| `QUICKSTART.md` | Fast-track setup guide and common commands |
| `INSTALL.md` | Step-by-step installation and configuration guide |

### Configuration Files

| File | Purpose |
|------|---------|
| `namespace.yaml` | Creates `argocd` namespace |
| `kustomization.yaml` | Kustomize configuration for managing all resources |
| `bootstrap/argocd-app.yaml` | ArgoCD as Application (self-managing) |

### Application Deployment

| File | Environment | Replicas | Purpose |
|------|-------------|----------|---------|
| `applications/app-dev.yaml` | Development | 1 | Testing with Stripe mock |
| `applications/app-staging.yaml` | Staging | 2 | Pre-production testing |
| `applications/app-prod.yaml` | Production | 3+ | Live environment |
| `applicationset/app-environments.yaml` | All | Varies | Multi-env deployment |

### Access Control & Projects

| File | Purpose |
|------|---------|
| `projects/projects.yaml` | Defines ArgoCD Projects with RBAC policies |

### Repository & Credentials

| File | Purpose |
|------|---------|
| `repositories/repositories.yaml` | Git repository and Docker registry credentials |

### Secrets Management

| File | Purpose |
|------|---------|
| `secrets/secrets-dev.yaml` | Development secrets (safe to commit) |
| `secrets/secrets-staging-prod.yaml` | Template for production secrets (DON'T commit real values) |
| `secrets/external-secrets-example.yaml` | Examples for AWS/Vault/Azure secret management |

### ArgoCD Configuration

| File | Purpose |
|------|---------|
| `config/argocd-cm.yaml` | RBAC policies, notifications, server settings |

### Scripts

| File | Purpose |
|------|---------|
| `setup.sh` | Automated installation and configuration script |

## 🔑 Key Features

### Multi-Environment Support
- **Development**: Auto-sync, Stripe mock enabled, 1 replica
- **Staging**: Auto-sync, real Stripe API (test keys), 2 replicas
- **Production**: Manual/auto-sync option, live Stripe keys, 3+ replicas, anti-affinity

### GitOps Workflow
- Declarative infrastructure in Git
- Automatic synchronization with repository
- Easy rollback to previous deployments
- Audit trail of all changes

### Security
- RBAC with role-based access control
- Separation of concerns (dev/staging/prod)
- Secret management via Sealed Secrets or External Secrets
- Production project with restricted access

### High Availability
- Multiple replicas for applications
- Pod anti-affinity rules (production)
- Persistent storage for databases
- Health checks and readiness probes

### Monitoring & Observability
- Sync status tracking
- Application health monitoring
- Event logging
- Integration with monitoring systems

## 🔄 GitOps Workflow

```
develop branch → app-dev → testing
    ↓
main branch → app-staging → pre-production testing
    ↓
v1.0.0 tag → app-prod → production
```

1. **Developer** pushes code to feature branch
2. **CI/CD** builds and tests the code
3. **PR Review** checks code quality
4. **Merge** to `develop` branch
5. **ArgoCD** automatically deploys to `merchant-payout-dev`
6. **Testing** validates changes in development
7. **Merge** to `main` branch (after approval)
8. **ArgoCD** automatically deploys to `merchant-payout-staging`
9. **Release** via Git tag (e.g., `v1.0.0`)
10. **ArgoCD** automatically deploys to `merchant-payout-prod`

## 🛠️ Common Operations

### Deploy to Development
```bash
kubectl apply -f argocd/applications/app-dev.yaml
argocd app sync merchant-payout-dev
```

### Deploy to All Environments
```bash
kubectl apply -k argocd/
```

### Check Application Status
```bash
argocd app list
argocd app get merchant-payout-dev
```

### Manual Sync
```bash
argocd app sync merchant-payout-staging --force
```

### Rollback Deployment
```bash
argocd app history merchant-payout-prod
argocd app rollback merchant-payout-prod 1
```

### View Logs
```bash
kubectl logs -f -n argocd deployment/argocd-server
argocd app logs merchant-payout-dev
```

## 📦 Prerequisites

- Kubernetes 1.20+
- Helm 3.0+
- kubectl configured for cluster access
- GitHub repository access (or GitLab, Gitea, etc.)
- Docker Registry access (if using private images)

## ⚠️ Important Notes

### Secrets Management

1. **Development Secrets** (`secrets-dev.yaml`)
   - Contains test values
   - Safe to commit to Git
   - Used only in dev environment

2. **Production Secrets** (`secrets-staging-prod.yaml`)
   - Template provided (DO NOT commit real secrets)
   - Use Sealed Secrets or External Secrets Operator
   - Managed outside of Git

3. **Best Practices**
   - Never commit production passwords
   - Rotate secrets regularly
   - Use RBAC to restrict secret access
   - Audit all secret access

### Repository Configuration

Before deploying, update:

1. **Git Repository URL** (in all application manifests)
   ```yaml
   source:
     repoURL: https://github.com/YOUR-ORG/merchant-payout-system
   ```

2. **Credentials** (in `repositories/repositories.yaml`)
   ```yaml
   username: YOUR-GITHUB-USERNAME
   password: YOUR-GITHUB-TOKEN
   ```

3. **RBAC** (in `config/argocd-cm.yaml`)
   ```yaml
   g, your-github-team, role:developers
   ```

### Image Registry

Update image registry in application manifests:

```yaml
app:
  image:
    repository: your-registry/merchant-payout-system
    tag: latest
```

## 📚 Documentation

- **README.md**: Comprehensive guide with advanced topics
- **QUICKSTART.md**: Fast track setup and common commands
- **INSTALL.md**: Detailed step-by-step installation guide
- **setup.sh**: Interactive automated setup script

## 🔗 Integration Points

### GitHub Actions
```bash
# File: .github/workflows/argocd-deploy.yaml
# Automatically builds, pushes image, and triggers ArgoCD sync
```

### Helm Chart
```bash
# Location: helm/
# ArgoCD deploys this Helm chart with environment-specific values
```

### Kubernetes
```bash
# Multiple namespaces:
# - argocd (ArgoCD control plane)
# - merchant-payout-dev (Development)
# - merchant-payout-staging (Staging)
# - merchant-payout-prod (Production)
```

## 🚨 Troubleshooting

### Application Won't Sync
```bash
# Check ArgoCD logs
kubectl logs -f -n argocd deployment/argocd-application-controller

# Get application status
argocd app get merchant-payout-dev

# Describe application
kubectl describe application merchant-payout-dev -n argocd
```

### Repository Access Issues
```bash
# Update credentials
kubectl patch secret github-repo -n argocd \
  -p '{"stringData":{"password":"NEW-TOKEN"}}'

# Test connection
argocd repo list
```

### Pods Not Deploying
```bash
# Check namespace
kubectl get namespace merchant-payout-dev

# Check pod status
kubectl get pods -n merchant-payout-dev
kubectl describe pod <pod-name> -n merchant-payout-dev

# Check events
kubectl get events -n merchant-payout-dev
```

## 📞 Support

For issues:
1. Check `INSTALL.md` troubleshooting section
2. Review `README.md` for advanced topics
3. Check [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
4. Search [GitHub Issues](https://github.com/argoproj/argo-cd/issues)

## ✅ Deployment Checklist

- [ ] Update Git repository URLs
- [ ] Configure GitHub credentials
- [ ] Set up Sealed Secrets for production
- [ ] Configure RBAC policies
- [ ] Test development deployment
- [ ] Test staging deployment
- [ ] Test production deployment
- [ ] Set up webhooks
- [ ] Configure notifications (Slack, email)
- [ ] Set up monitoring and alerts
- [ ] Document environment variables
- [ ] Test rollback procedures
- [ ] Configure backup strategy
- [ ] Document incident procedures

## 🎯 Next Steps

1. Run `./argocd/setup.sh` for automated setup
2. Update repository URLs and credentials
3. Deploy to development environment
4. Test GitOps workflow
5. Set up production secrets
6. Configure monitoring and alerts
7. Document procedures for your team

---

**Last Updated**: July 2026
**Version**: 1.0.0
**Maintainer**: Development Team

