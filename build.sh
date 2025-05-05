#!/bin/bash

echo "Compiling..."
mkdir -p out 
javac -d out src/*.java

echo "Running..."
java -cp :out SketchpadUI
