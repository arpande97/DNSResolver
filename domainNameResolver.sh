#!/bin/bash

if [ "$#" -ne 1 ]; then
	echo "Usage: $0 <domain>"
	exit 1
fi

url="http://localhost:8080/resolver"
data="$1"

curl -X POST "$url" -d "$data" -H "Content-Type: text/plain"
