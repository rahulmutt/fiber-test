#!/usr/bin/env sh

set -e
cd src/main/eta
etlas clean
etlas configure --enable-uberjar-mode
etlas build
cd ../../..
mvn install:install-file -Dfile=src/main/eta/dist/build/threadring/threadring.jar -DgroupId=com.typelead -DartifactId=threadring -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

mvn clean install

