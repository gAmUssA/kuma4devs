SHELL=/bin/bash
THIS_MKFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
THIS_MKFILE_DIR := $(dir $(THIS_MKFILE_PATH))

include $(THIS_MKFILE_DIR)common/Makefile
include $(THIS_MKFILE_DIR)common/Makefile.gke

GCP_PROJECT_ID ?= $(shell gcloud config list --format 'value(core.project)')
GKE_BASE_MACHINE_TYPE ?= e2-standard-2

CLUSTER_NAME=kong-gateway-4-istio-mesh
CLUSTER_ZONE=us-central1-a

CURRENT_WORKING_DIR=$(shell pwd)
WHO_AM_I=$(shell whoami)
TIMESTAMP=$(shell date)

pad=$(printf '%0.1s' "-"{1..80})

KONG_MESH_VERSION ?= 2.2.0
KUMA_VERSION ?= 2.2.0
KONG_NAMESPACE ?= kong
KONG_HELM_RELEASE ?= kong
DEMO_NAMESPACE ?= boutique
LICENSE_PATH ?= ~/tmp/istio+kong

define print-prompt =
printf "\e[96m➜ \e[0m"
endef

define print-header =
printf "\n%-50s\n" $1 | tr ' ~' '- '
endef

.DEFAULT_GOAL := help

minikube-create-cluster:
	@$(call print-header,"creating new minikube cluster")
	@$(call print-prompt)
	minikube start --cpus 6 --memory 8Gb
	@echo ""

minikube-destroy-cluster:
	@$(call print-header,"deleting minikube cluster")
	@$(call print-prompt)
	minikube delete --all
	@echo ""

minikube-use-context:
	@$(call print-header,"switching to minikube context...")
	@$(call print-prompt)
	kubectl config use-context minikube
	@echo ""

kong-mesh-install:
	@echo "☸️  Installing Kuma 🐻‍❄️"

	if [ -d "./kong-mesh-${KONG_MESH_VERSION}" ]; then \
        echo "Found ./kong-mesh-${KONG_MESH_VERSION}"; \
        install -m 755 ./kong-mesh-${KONG_MESH_VERSION}/bin/kumactl ~/bin/kumactl ;\
    else \
        echo "No Kong Mesh installation found"; \
        echo "☸️  Downloading 🦍 Kong Mesh 𐄳 ..." ;\
        curl -L https://docs.konghq.com/mesh/installer.sh | sh - ;\
    fi
	@echo "${BLUE}𐄳${RESET} 🐻‍❄️ using kumactl..."
	which kumactl
	@echo "${BLUE}𐄳${RESET} Installing Kuma Control Plane..."
	kumactl install control-plane --license-path ${LICENSE_PATH}/license.json | kubectl apply -f -
	@echo ""

kuma-install: check-dependencies
	@echo "☸️  Installing Kuma 🐻‍❄️"

	if [ -d "./kuma-${KUMA_VERSION}" ]; then \
        echo "Found ./kuma-${KUMA_VERSION}"; \
        install -m 755 ./kuma-${KUMA_VERSION}/bin/kumactl ~/bin/kumactl ;\
    else \
        echo "No kuma installation found"; \
        echo "☸️  Downloading 🐻‍❄️ Kuma 𐄳 ..." ;\
        curl -L https://kuma.io/installer.sh | VERSION=${KUMA_VERSION} sh - ;\
		install -m 755 ./kuma-${KUMA_VERSION}/bin/kumactl ~/bin/kumactl ;\
    fi
	@echo "${BLUE}𐄳${RESET} 🐻‍❄️ using kumactl..."
	which kumactl
	@echo "${BLUE}𐄳${RESET} Installing Kuma Control Plane..."
	kubectl config current-context
	kumactl install control-plane | kubectl apply -f -
	@echo ""

mesh-validate: check-dependencies
	kubectl get pods -n kuma-system
	kumactl inspect meshes

kong-mesh-cp-ui-pf: check-dependencies
	kubectl port-forward svc/kong-mesh-control-plane -n kong-mesh-system 5681:5681 &  

kuma-cp-ui-pf: check-dependencies 
	kubectl port-forward svc/kuma-control-plane -n kuma-system 5681:5681 & 

kong-mesh-observability-ui: check-dependencies
	kubectl port-forward svc/grafana -n mesh-observability 3000:3000 

kong-mesh-policy: check-dependencies
	kubectl apply -f ./mesh/mesh.yaml
	#kubectl apply -k ./mesh
			
kong-gateway-install: check-dependencies
	@echo "☸️  Installing Kong 🦍 to ${GREEN}${KONG_NAMESPACE}${RESET} namespace..."
	kubectl create namespace ${KONG_NAMESPACE}
	kubectl label namespace ${KONG_NAMESPACE} kuma.io/sidecar-injection=enabled
	helm repo add kong https://charts.konghq.com && helm repo update
	helm install -n ${KONG_NAMESPACE} ${KONG_HELM_RELEASE} kong/kong
	kubectl -n ${KONG_NAMESPACE} wait --for=condition=Available deployment ${KONG_HELM_RELEASE}-kong
	@echo ""

kong-gateway-uninstall: check-dependencies
	@echo "☸️  Uninstalling Kong from ${GREEN}${KONG_NAMESPACE}${RESET} namespace..."
	helm uninstall -n ${KONG_NAMESPACE} ${KONG_HELM_RELEASE}
	kubectl delete namespace ${KONG_NAMESPACE}
	@echo ""

obs-tools-install: check-dependencies
	@echo "☸️  Installing Observability Tools - Prometheus 🔥, Grafana 📊, Jaeger, Loki"
	kumactl install observability | kubectl apply -f -
	@echo ""

obs-tools-uninstall: check-dependencies
	@echo "❌ Uninstalling Observability Tools"
	kumactl install  observability | kubectl delete -f -
	@echo ""
	
skaffold-run: check-dependencies
	@echo "☕️ Deploy Apps with Skaffold"
	skaffold run -n mesh4java
	@echo ""

skaffold-delete: check-dependencies
	@echo "🔥 uninstall Apps with "
	skaffold delete -n mesh4java
	@echo ""

	
help:
	@echo ""
	@echo "    ${BLUE}:: ${GREEN}Kuma 🐻‍❄️ Service Mesh${RESET} for ☕️ Java Developers ${BLUE}::${RESET}"
	@echo ""
	@echo "The demo setup demonstrates how to use Kong Gateway and Kong Ingress Controller with Istio Service Mesh"
	@echo ""
	@echo "${BLUE}Targets${RESET} (in order to execute:)"
	@echo "${BLUE}------ minikube targets -----------------------------------------------------${RESET}"
	@echo "  | ${BLUE}minikube-create-cluster${RESET}:		create local minikube cluster"
	@echo "  | ${BLUE}minikube-delete-cluster${RESET}:		destroy local minikube cluster"
	@echo "  | ${BLUE}minikube-use-context${RESET}:		use minikube context as kubectl api endpoint"
	@echo "${BLUE}------ kuma targets -----------------------------------------------------${RESET}"
	@echo "  | ${BLUE}kuma-install${RESET}:			download and install Kuma, kumactl, Kuma Controll Plane"
	@echo "  | ${BLUE}mesh-validate${RESET}:			validate installation of Kuma CP"
	@echo "${BLUE}------ apps targets -----------------------------------------------------${RESET}"
	@echo "  | ${BLUE}skaffold-run${RESET}:			deploy apps"
	@echo "  | ${BLUE}skaffold-delete${RESET}:			undeploy apps"
	@echo "${BLUE}-----------------------------------------------------------------------------${RESET}"
	@echo "If something doesn't work, report at https://github.com/gAmUssA/kuma4java/issues"
