#!/bin/bash

export PATH="$PATH:$(go env GOPATH)/bin"
MY_PATH=`dirname "$0"`
PROTO_PATH="$MY_PATH/proto"

# Alert API Gateway

ALERT_API_GATEWAY_PATH="$MY_PATH/alert_api_gateway"

mkdir -p $ALERT_API_GATEWAY_PATH/proto
protoc --go_out=$ALERT_API_GATEWAY_PATH/proto \
    --go_opt=paths=source_relative \
    --go-grpc_out=$ALERT_API_GATEWAY_PATH/proto \
    --go-grpc_opt=paths=source_relative \
    --proto_path=$PROTO_PATH \
    $PROTO_PATH/alert_api_gateway.proto
