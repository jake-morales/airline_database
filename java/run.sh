#! /bin/bash
DBNAME=$DB
PORT=$PGPORT
USER=$USER

# Example: source ./run.sh flightDB 5432 user
java -cp lib/*:bin/ DBproject $DBNAME $PORT $USER
