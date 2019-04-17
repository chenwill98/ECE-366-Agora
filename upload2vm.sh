
#!/bin/bash

## Used to push all our code onto the VM and then shh into it. 
## Requires one to input the password like 4 times.
# push the backend http server jar onto the VM
cd ./Backend/apolloBackend
mvn clean package
cd ../..
scp -P 5122  ./Backend/apolloBackend/target/apollo-backend.jar cooper@199.98.27.114:agora

# push the sql file onto the VM
# scp -P 5122  ./Backend/MySQL/Database_Creation.SQL cooper@199.98.27.114:agora


# push the frontend build folder to the VM
cd agora
yarn build
cd ..
scp -P 5122 -r ./agora/build cooper@199.98.27.114:agora

# Command to run the backend http server in the VM:
# scp -P 5122 -r ./run_backend.sh cooper@199.98.27.114:agora

# ssh into VM
ssh -p5122 cooper@199.98.27.114



# USERNAME="root" PASSWORD="cuece366agora" JDBC="jdbc:mysql://199.98.27.114/AgoraDB?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" MYSQL_HOST=199.98.27.114 java -jar apollo-backend.jar & > apollo_log.txt


