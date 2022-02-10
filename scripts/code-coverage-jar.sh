#!/usr/bin/env sh

PATH_JACOCO_CLI_JAR=""
PATH_JCS_SRC="src"
PATH_JCS_JAR="resources/jcs-1.3.jar"
PATH_JSC_FAT_JAR="resources/jcs-fat-1.3.jar/"
PROJECT_HOME=".."

cd ${PROJECT_HOME} || exit

java -jar "${PATH_JACOCO_CLI_JAR}"/jacococli.jar instrument ${PATH_JCS_JAR} --dest ${PATH_JSC_FAT_JAR}

mkdir -p target/jacoco-gen/jcs-coverage/

java -jar "${PATH_JACOCO_CLI_JAR}"/jacococli.jar report target/jacoco.exec --classfiles ${PATH_JCS_JAR} --sourcefiles ${PATH_JCS_SRC} --html target/jacoco-gen/jcs-coverage/ --xml target/jacoco-gen/jcs-coverage/coverage.xml --csv target/jacoco-gen/jcs-coverage/coverage.csv
