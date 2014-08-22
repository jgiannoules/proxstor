#!/bin/sh
model=$1
if [ -z "$model" ]; then
		echo "please specify a model number"
		return
fi
dot -Tpng $model.gv > $model.png
echo consult $model.png
