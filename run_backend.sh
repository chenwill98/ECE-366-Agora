#!/bin/bash

## Script used to run the http server in the backend.

echo "Http server and logging output to 'log_output_apollo.txt'."

USERNAME="root" PASSWORD="cuece366agora" JDBC="jdbc:mysql://localhost/AgoraDB?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" java -jar apollo-backend.jar 2> lot_error_apollog.txt > log_output_apollo.txt &





