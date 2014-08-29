#!/bin/bash

# $1 firstName
# $2 lastName
# $3 address
# $4 count

firstName=$1
lastName=$2
address=$3
counter=$4

echo "adding $firstName $lastName (address $address) $counter times"

while [ $counter -gt 0 ]; do
		curl -s -H "Content-Type: application/json" -H "Accept: application/json" -d "{\"firstName\":\"$firstName\",\"lastName\":\"$lastName\", \"email\":\"$address\"}" http://localhost:8080/proxstor-webapp/api/users > /dev/null
		echo $counter
		let counter-=1
done
