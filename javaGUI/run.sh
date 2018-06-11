#! /bin/bash
DBNAME=$1
PORT=$2
USER=$3

# Example: source ./run.sh jake_DB 9999 jake
java -cp lib/*:bin/ DBproject $DBNAME $PORT $USER
