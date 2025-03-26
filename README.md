# Geolocation Service

![CI](https://github.com/mattmoore/geolocation/actions/workflows/ci.yml/badge.svg)

![Logo](docs/images/satelite-icon-vector.png)

Geolocation service for operations with addresses and GPS coordinates. There is a docker compose for both the geolocation service and Grafana Loki for logs, as well as Jaeger for traces.

## Start geolocation

First, you'll need to install the Docker driver for Grafana Loki:

```shell
docker plugin install grafana/loki-docker-driver:3.3.2-arm64 --alias loki --grant-all-permissions
```

```shell
docker compose -f docker/geolocation/docker-compose.yml up -d
```

This will also start Jaeger for traces, and the Grafana stack for easy observability. Once the Grafana stack is fully running, you can access the web UI at http://localhost:3000. The default username/password is admin/admin.

## Stop geolocation:

```shell
docker compose -f docker/geolocation/docker-compose.yml down
```

## Curl examples

Request GPS coordinates for an address:

```shell
curl -v -X POST http://localhost:8080/api/coords -d '{"street": "123 Anywhere St.", "city": "New York", "state": "NY"}'
```

Create a new address:

```shell
curl -v -X POST http://localhost:8080/api/coords/new -d '{"id": 3, "street": "123 Anywhere St.", "city": "New York", "state": "NY", "coords": { "lat": 10, "lon": 10 } }'
```

## K8s/kind

To fully start:

```shell
# Create the kind cluster
kind create cluster --config kind-cluster-config.yml

# Build and load the geolocation image
kind load docker-image geolocation:0.1.0

# Can monitor the images on kind with
docker exec -it $(kind get clusters | head -1)-control-plane crictl images

# Deploy
kubectl apply -f k8s

# To test:
curl -v -X POST http://localhost:30000/api/coords/new -H 'content-type: application/json' -d '{"id": 33, "street": "123 Anywhere St.", "city": "New York", "state": "NY", "coords": { "lat": 10, "lon": 10 } }'
```

Delete things:

```shell
# Delete the cluster
kind delete clusters kind

# Delete k8s config:
kubectl delete -f k8s
```

## Grafana dashboards

PostgreSQL via postgres_exporter: https://grafana.com/grafana/dashboards/9628-postgresql-database/
