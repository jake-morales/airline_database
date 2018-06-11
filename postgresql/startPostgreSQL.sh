#! /bin/bash
folder=/tmp/$USER
export PGDATA=$folder/myDB/data
export PGSOCKETS=$folder/myDB/sockets

echo $folder

#Clear folder
rm -rf $folder

#Initialize folders
mkdir $folder
mkdir $folder/myDB
mkdir $folder/myDB/data
mkdir $folder/myDB/sockets
sleep 1
#cp ../data/*.csv $folder/myDB/data

#Initialize DB -- changed path 
PATH=$PATH:/usr/lib/postgresql/10/bin/
EXPORT PATH
initdb

sleep 1
#Start folder
export PGPORT=9996
pg_ctl -o "-c unix_socket_directories=$PGSOCKETS -p $PGPORT" -D $PGDATA -l $folder/logfile start

