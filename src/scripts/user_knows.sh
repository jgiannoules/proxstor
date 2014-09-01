#!/bin/bash

# $1 userId
# $2 userId2 
# $3 strength 

userId=$1
userId2=$2
strength=$3
count=$4

for i in `seq 1 $count`; do
		curl -d "" http://localhost:8084/proxstor-webapp/api/users/$userId/knows/$strength/$userId2
		echo
done
