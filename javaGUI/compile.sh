#! /bin/bash
rm -rf bin/*.class
javac -cp ".;lib/postgresql-42.1.4.jar;" src/DBproject.java src/GUI.java src/Popup.java -d bin/
