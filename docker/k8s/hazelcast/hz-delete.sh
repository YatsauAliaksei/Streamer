#!/usr/bin/env bash

microk8s.kubectl delete deployment hz
microk8s.kubectl delete deployment hz-man

microk8s.kubectl delete service hz
microk8s.kubectl delete service hz-man
