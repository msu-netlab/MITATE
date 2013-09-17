#!/bin/sh
#echo "Enter your MITATE username: ";
#read username;
#echo "Enter your password: ";
#read password;
if [ "$1" == 'upload' ] 
then
	curl -k -ssl3 -F "username=test" -F "password=test" -F file=@$2 https://mitate.cs.montana.edu/mitate_upload_experiment.php
elif [ "$1" == 'delete' ] 
then
	curl -k -ssl3 -F "username=test" -F "password=test" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_delete_experiment.php
elif [ "$1" == 'query' ]
then
	curl -k -ssl3 -F "username=test" -F "password=test" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_query_experiment.php
elif [ "$1" == 'init' ]
then
	curl -k -ssl3 -F "username=test" -F "password=test" https://mitate.cs.montana.edu/mitate_initialize_db.php
elif [ "$1" == 'update' ]
then
	echo "This experiment will be made public. Are you sure?(y/n))";
	read update_response;
	if [ $update_response == 'y' ]
	then
		curl -k -ssl3 -F "username=test" -F "password=test" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_update_experiment.php
	fi
else
	echo "Missing operation";
fi