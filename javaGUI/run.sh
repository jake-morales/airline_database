#! /bin/bash
DBNAME="$USER"_DB
PORT=$PGPORT
USER=$USER

# Example: source ./run.sh jake_DB 9999 jake
java -cp lib/*:bin/ DBproject $DBNAME $PORT $USER
