#!/usr/bin/env bash

microk8s.kubectl delete pvc hz-claim
microk8s.kubectl delete pv hz-volume
microk8s.kubectl delete sc hz-local-storage
