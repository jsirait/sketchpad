#!/bin/bash

echo "Compiling..."
mkdir -p out
javac -cp lib/cassowary.jar -d out src/*.java

echo "Running..."
java -cp lib/cassowary.jar:out SketchpadUI
