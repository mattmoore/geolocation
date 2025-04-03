#.DEFAULT_GOAL := help

.PHONY: create-cluster
create-cluster:
	kind create cluster --config kind-cluster-config.yml
	kubectl create namespace geolocation
	kubectl create namespace monitoring
	helm upgrade --install --set args={--kubelet-insecure-tls} metrics-server metrics-server/metrics-server --namespace kube-system
	helm install kube-state-metrics kube-state-metrics

.PHONY: delete-cluster
delete-cluster:
	kind delete clusters geolocation

.PHONY: install
install:
	kind load docker-image geolocation:0.1.0 --name geolocation
	helm install postgres postgres
	helm install postgres-exporter postgres-exporter
	helm install loki loki
	helm install jaeger jaeger
	helm install prometheus prometheus
	helm install otel-collector otel-collector
	helm install grafana grafana
	helm install geolocation geolocation

.PHONY: uninstall
uninstall:
	kind load docker-image geolocation:0.1.0 --name geolocation
	helm uninstall postgres
	helm uninstall postgres-exporter
	helm uninstall loki
	helm uninstall jaeger
	helm uninstall prometheus
	helm uninstall otel-collector
	helm uninstall grafana
	helm uninstall geolocation
