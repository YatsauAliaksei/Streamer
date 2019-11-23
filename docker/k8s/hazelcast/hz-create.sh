#!/usr/bin/env bash

microk8s.kubectl create -f hazelcast-management-center.yaml
microk8s.kubectl create -f hz-man-service.yaml

microk8s.kubectl create -f hazelcast.yaml
microk8s.kubectl create -f hz-service.yaml
