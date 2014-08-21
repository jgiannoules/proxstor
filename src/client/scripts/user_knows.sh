#!/bin/bash

# $1 userId
# $2 lower range
# $3 upper range

userId=$1
lower=$2
upper=$3

for i in `seq $lower $upper`; do
		curl -d "" http://localhost:8084/proxstor-webapp/api/users/$userId/knows/$i
		echo
done
