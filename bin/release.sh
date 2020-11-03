#!/usr/bin/env bash 

V=$1
git tag -a $V -m "version $V"
git push origin $V 
