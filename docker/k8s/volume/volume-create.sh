#!/usr/bin/env bash
microk8s.kubectl create -f hz-sc.yaml
microk8s.kubectl create -f hz-pv.yaml
microk8s.kubectl create -f hz-pvc.yaml
