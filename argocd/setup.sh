#!/bin/bash

# ArgoCD Setup Script
# This script automates the installation and configuration of ArgoCD for the Merchant Payout System

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ARGOCD_NAMESPACE="argocd"
ARGOCD_VERSION="7.3.3"
ARGOCD_SERVER_URL="${ARGOCD_SERVER_URL:-}"
GITHUB_REPO_URL="${GITHUB_REPO_URL:-https://github.com/your-org/merchant-payout-system}"
GITHUB_USERNAME="${GITHUB_USERNAME:-}"
GITHUB_TOKEN="${GITHUB_TOKEN:-}"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    log_success "kubectl found"

    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed"
        exit 1
    fi
    log_success "helm found"

    if ! command -v argocd &> /dev/null; then
        log_warning "argocd CLI is not installed, installing..."
        curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
        chmod +x /usr/local/bin/argocd
        log_success "argocd CLI installed"
    else
        log_success "argocd CLI found"
    fi

    # Check cluster access
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    log_success "Connected to Kubernetes cluster"
}

create_namespace() {
    log_info "Creating ArgoCD namespace..."

    if kubectl get namespace $ARGOCD_NAMESPACE &> /dev/null; then
        log_warning "Namespace $ARGOCD_NAMESPACE already exists"
    else
        kubectl create namespace $ARGOCD_NAMESPACE
        log_success "Namespace $ARGOCD_NAMESPACE created"
    fi
}

install_argocd_helm() {
    log_info "Installing ArgoCD using Helm..."

    # Add Helm repository
    helm repo add argo https://argoproj.github.io/argo-helm
    helm repo update

    # Check if release exists
    if helm list -n $ARGOCD_NAMESPACE | grep -q argocd; then
        log_warning "ArgoCD Helm release already exists"
        read -p "Do you want to upgrade? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            helm upgrade argocd argo/argo-cd \
                --namespace $ARGOCD_NAMESPACE \
                --values argocd/bootstrap/argocd-app.yaml
            log_success "ArgoCD upgraded"
        fi
    else
        helm install argocd argo/argo-cd \
            --namespace $ARGOCD_NAMESPACE \
            --values argocd/bootstrap/argocd-app.yaml
        log_success "ArgoCD installed"
    fi
}

configure_repositories() {
    log_info "Configuring Git and Docker repositories..."

    if [ -z "$GITHUB_USERNAME" ] || [ -z "$GITHUB_TOKEN" ]; then
        log_warning "GitHub credentials not provided, skipping repository configuration"
        log_info "Configure manually using: kubectl apply -f argocd/repositories/repositories.yaml"
    else
        # Update repository credentials
        kubectl create secret generic github-repo \
            --from-literal=type=git \
            --from-literal=url=$GITHUB_REPO_URL \
            --from-literal=username=$GITHUB_USERNAME \
            --from-literal=password=$GITHUB_TOKEN \
            -n $ARGOCD_NAMESPACE \
            --dry-run=client -o yaml | \
            kubectl apply -f -

        kubectl label secret github-repo \
            argocd.argoproj.io/secret-type=repository \
            -n $ARGOCD_NAMESPACE \
            --overwrite

        log_success "Git repository credentials configured"
    fi
}

apply_configurations() {
    log_info "Applying ArgoCD configurations..."

    # Wait for ArgoCD to be ready
    log_info "Waiting for ArgoCD to be ready..."
    kubectl wait --for=condition=available --timeout=300s \
        deployment/argocd-server -n $ARGOCD_NAMESPACE

    # Apply projects
    kubectl apply -f argocd/projects/projects.yaml
    log_success "Projects configured"

    # Apply configuration
    kubectl apply -f argocd/config/argocd-cm.yaml
    log_success "Configuration applied"

    # Apply secrets for development
    kubectl apply -f argocd/secrets/secrets-dev.yaml
    log_success "Development secrets configured"
}

deploy_applications() {
    log_info "Deploying ArgoCD Applications..."

    # Get deployment choice
    echo "Select deployment option:"
    echo "1) Deploy development only"
    echo "2) Deploy development and staging"
    echo "3) Deploy all environments"
    echo "4) Skip (deploy manually later)"
    read -p "Enter choice (1-4): " -n 1 -r
    echo

    case $REPLY in
        1)
            kubectl apply -f argocd/applications/app-dev.yaml
            log_success "Development application deployed"
            ;;
        2)
            kubectl apply -f argocd/applications/app-dev.yaml
            kubectl apply -f argocd/applications/app-staging.yaml
            log_success "Development and staging applications deployed"
            ;;
        3)
            kubectl apply -f argocd/applications/
            log_success "All applications deployed"
            ;;
        4)
            log_info "Skipping application deployment"
            ;;
        *)
            log_error "Invalid choice"
            ;;
    esac
}

wait_for_services() {
    log_info "Waiting for services to be ready..."

    # Wait for ArgoCD to be fully operational
    kubectl rollout status deployment/argocd-server -n $ARGOCD_NAMESPACE --timeout=300s

    log_success "ArgoCD is ready"
}

get_admin_password() {
    log_info "Retrieving ArgoCD admin password..."

    # Wait for secret to be created
    sleep 5

    if kubectl get secret argocd-initial-admin-secret -n $ARGOCD_NAMESPACE &> /dev/null; then
        ADMIN_PASSWORD=$(kubectl -n $ARGOCD_NAMESPACE get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
        log_success "Admin password: $ADMIN_PASSWORD"
        echo ""
        echo "Save this password and change it after first login!"
    else
        log_warning "Admin secret not found, please retrieve password manually"
    fi
}

display_access_instructions() {
    log_info "ArgoCD has been successfully installed!"
    echo ""
    echo -e "${BLUE}=== ACCESS INSTRUCTIONS ===${NC}"
    echo ""
    echo "1. Port forward to ArgoCD:"
    echo "   kubectl port-forward -n $ARGOCD_NAMESPACE svc/argocd-server 8080:443"
    echo ""
    echo "2. Access ArgoCD UI:"
    echo "   https://localhost:8080"
    echo ""
    echo "3. Login credentials:"
    echo "   Username: admin"
    echo "   Password: (see above)"
    echo ""
    echo "4. Check application status:"
    echo "   argocd app list"
    echo "   argocd app get merchant-payout-dev"
    echo ""
    echo "5. Monitor applications:"
    echo "   kubectl get applications -n $ARGOCD_NAMESPACE -w"
    echo ""

    if [ -n "$ARGOCD_SERVER_URL" ]; then
        echo "6. Login via CLI:"
        echo "   argocd login $ARGOCD_SERVER_URL --username admin --password $ADMIN_PASSWORD"
        echo ""
    fi

    echo -e "${BLUE}=== NEXT STEPS ===${NC}"
    echo ""
    echo "1. Change admin password:"
    echo "   argocd account update-password --account admin"
    echo ""
    echo "2. Configure GitHub OAuth (optional):"
    echo "   kubectl patch configmap argocd-cm -n $ARGOCD_NAMESPACE -p '{...}'"
    echo ""
    echo "3. Set up webhooks in GitHub repository settings"
    echo ""
    echo "4. Configure Slack notifications (optional):"
    echo "   kubectl patch configmap argocd-notifications-cm -n $ARGOCD_NAMESPACE -p '{...}'"
    echo ""
}

cleanup_old_resources() {
    log_info "Checking for and cleaning up old ArgoCD resources..."

    # Delete old manifest-based installation if it exists
    if kubectl get namespace argocd &> /dev/null; then
        if kubectl get deployment argocd-server -n $ARGOCD_NAMESPACE &> /dev/null; then
            log_warning "Old ArgoCD deployment found"
            read -p "Delete old manifest-based installation? (y/n) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                # Keep persistent data but remove old deployments
                log_info "Backing up and removing old installation..."
                # kubectl delete -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml -n $ARGOCD_NAMESPACE
            fi
        fi
    fi
}

main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║   ArgoCD Setup for Merchant Payout System                 ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""

    log_info "Starting ArgoCD installation..."
    echo ""

    check_prerequisites
    echo ""

    create_namespace
    echo ""

    cleanup_old_resources
    echo ""

    install_argocd_helm
    echo ""

    wait_for_services
    echo ""

    configure_repositories
    echo ""

    apply_configurations
    echo ""

    deploy_applications
    echo ""

    get_admin_password
    echo ""

    display_access_instructions

    log_success "ArgoCD installation completed!"
}

# Run main function
main "$@"

