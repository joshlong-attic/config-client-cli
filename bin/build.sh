#!/usr/bin/env bash
export GITHUB_ENV=${GITHUB_ENV:-$HOME/Desktop/out}
mvn -Pnative -DskipTests=true clean package
./target/config-client guest password infrastructure production http://localhost:8888 $GITHUB_ENV
