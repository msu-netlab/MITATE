#!/bin/sh
echo "Enter your MITATE username: ";
read username;
echo "Enter your password: ";
read password;
if [ "$1" == 'upload' ] 
then
	curl -k -ssl3 -F "username=$username" -F "password=$password" -F file=@$2 https://mitate.cs.montana.edu/mitate_upload_experiment.php
elif [ "$1" == 'delete' ] 
then
	curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_delete_experiment.php
fi