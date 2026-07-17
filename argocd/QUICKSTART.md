# ArgoCD Quick Start

## 5-Minute Setup

### 1. Install ArgoCD
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### 2. Get Admin Password
```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### 3. Port Forward
```bash
kubectl port-forward -n argocd svc/argocd-server -n argocd 8080:443
```

### 4. Login
- Open https://localhost:8080
- Username: `admin`
- Password: (from step 2)

### 5. Add Your Repository
```bash
argocd repo add https://github.com/your-org/merchant-payout-system \
  --username <github-username> \
  --password <github-token>
```

### 6. Deploy Application
```bash
kubectl apply -f argocd/applications/app-dev.yaml
```

### 7. Monitor Sync
```bash
argocd app get merchant-payout-dev --watch
```

---

## Common Commands

### View Applications
```bash
argocd app list
```

### Sync Application
```bash
argocd app sync merchant-payout-dev
```

### View Application Status
```bash
argocd app get merchant-payout-dev
argocd app status merchant-payout-dev
```

### View Application Logs
```bash
argocd app logs merchant-payout-dev
```

### Rollback Application
```bash
argocd app history merchant-payout-dev
argocd app rollback merchant-payout-dev 0
```

### Delete Application
```bash
argocd app delete merchant-payout-dev
```

### Manual Refresh
```bash
argocd app refresh merchant-payout-dev
```

---

## Update Git Repository URL

Before deploying, update these files with your repository information:

1. **argocd/applications/app-*.yaml**
   ```yaml
   source:
     repoURL: https://github.com/YOUR-ORG/merchant-payout-system
   ```

2. **argocd/repositories/repositories.yaml**
   ```yaml
   stringData:
     url: https://github.com/YOUR-ORG/merchant-payout-system
     username: YOUR-USERNAME
     password: YOUR-GITHUB-TOKEN
   ```

3. **argocd/config/argocd-cm.yaml** (RBAC)
   ```yaml
     g, your-github-team, role:developers
   ```

---

## Deploy All Environments

### One-Command Deploy
```bash
# Apply all ArgoCD configurations
kubectl apply -f argocd/

# Or step by step
kubectl apply -f argocd/namespace.yaml
kubectl apply -f argocd/projects/projects.yaml
kubectl apply -f argocd/repositories/repositories.yaml
kubectl apply -f argocd/secrets/secrets-dev.yaml
kubectl apply -f argocd/config/argocd-cm.yaml
kubectl apply -f argocd/applications/
```

### Watch All Applications Sync
```bash
watch kubectl get applications -n argocd
```

---

## Access Applications

### Development
```bash
kubectl port-forward -n merchant-payout-dev svc/merchant-payout-system 8080:8080
# Access: http://localhost:8080
```

### Database
```bash
kubectl port-forward -n merchant-payout-dev svc/postgresql 5432:5432
# Connect: psql -h localhost -U postgres -d payout_system
```

### Kafka
```bash
kubectl port-forward -n merchant-payout-dev svc/kafka 9092:9092
```

---

## Troubleshooting

### Check Pod Status
```bash
kubectl get pods -n argocd
kubectl get pods -n merchant-payout-dev
```

### View Events
```bash
kubectl get events -n argocd --sort-by='.lastTimestamp'
```

### Check Application Sync Status
```bash
argocd app get merchant-payout-dev
```

### View ArgoCD Logs
```bash
kubectl logs -f -n argocd deployment/argocd-server
kubectl logs -f -n argocd deployment/argocd-application-controller
```

### Describe Application
```bash
kubectl describe application merchant-payout-dev -n argocd
```

---

## Production Checklist

- [ ] Update Git repository URLs in all manifests
- [ ] Configure GitHub credentials in `argocd/repositories/repositories.yaml`
- [ ] Set up Sealed Secrets for staging/prod credentials
- [ ] Configure Ingress for ArgoCD and applications
- [ ] Set up RBAC groups in `argocd/config/argocd-cm.yaml`
- [ ] Enable webhook notifications (GitHub, Slack, etc.)
- [ ] Configure auto-sync policies per environment
- [ ] Set up monitoring and alerts
- [ ] Document environment-specific configurations
- [ ] Test rollback procedures

---

## Next Steps

1. Read [README.md](./README.md) for comprehensive documentation
2. Customize values in `argocd/applications/app-*.yaml`
3. Set up secret management (Sealed Secrets or External Secrets)
4. Configure GitOps workflow with branching strategy
5. Set up monitoring and alerts

---

## Resources

- [ArgoCD Docs](https://argo-cd.readthedocs.io/)
- [GitOps Guide](https://www.weave.works/technologies/gitops/)
- [Helm Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

