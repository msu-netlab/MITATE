#!/bin/sh
userLoggedIn=1
if [ "$1" == 'logout' ]
then
	touch user.txt
	chmod 777 user.txt
	echo "" > user.txt
	echo "You are now logged out."
	userLoggedIn=0
fi
if [ "$userLoggedIn" == 1 ]
then
	ifUserIsValid='false'
	touch user.txt
	value=`cat user.txt`;
	encrypted_username=`echo $value | cut -d \: -f 1`
	encrypted_password=`echo $value | cut -d \: -f 2`
	if [ "$encrypted_username" != '' -a "$encrypted_password" != '' ]
	then
		username=`curl -k -ssl3 -F "string=$encrypted_username" https://mitate.cs.montana.edu/decrypt_user.php`
		password=`curl -k -ssl3 -F "string=$encrypted_password" https://mitate.cs.montana.edu/decrypt_user.php`
	else
		username=$encrypted_username
		password=$encrypted_password
	fi
	ifUserIsValid=`curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/validate_user.php`
	if [ "$ifUserIsValid" == 'true' ]
	then
		chmod 777 user.txt
		echo "$encrypted_username:$encrypted_password" > user.txt;
		chmod 444 user.txt
		isValid=1;
	else
		isValid=0;
		echo "Enter your MITATE username: ";
		read username;
		echo "Enter your password: ";
		read -s password;
		ifUserIsValid=`curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/validate_user.php`
		if [ "$ifUserIsValid" == 'true' ]
		then
			encrypted_username=`curl -k -ssl3 -F "string=$username" https://mitate.cs.montana.edu/encrypt_user.php`
			encrypted_password=`curl -k -ssl3 -F "string=$password" https://mitate.cs.montana.edu/encrypt_user.php`
			chmod 777 user.txt
			echo "$encrypted_username:$encrypted_password" > user.txt;
			chmod 444 user.txt
			isValid=1;
		else
			isValid=0;
			echo "Invalid account credentials."
		fi
	fi
	if [ "$isValid" == 1 ]
	then
		if [ "$1" == 'upload' -a "$2" != '' ] 
		then
			echo "Please wait while we process your file..."
			result=`curl -k -ssl3 -F "username=$username" -F "password=$password" -F file=@$2 https://mitate.cs.montana.edu/mitate_upload_experiment.php`
			if [ "$result" != 'Invalid account credentials.' ]
			then
				echo $result >> user_experiment_list.txt
				echo "Your experiment with ID: $result has been uploaded."
			fi
		elif [ "$1" == 'delete' -a "$2" != '' ] 
		then
			echo "This experiment will be deleted. Are you sure?(y/n)";
			read delete_response;
			if [ $delete_response == 'y' ]
			then
				curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_delete_experiment.php
				touch temp_user_delete_experiment.txt
				grep -v $2 user_experiment_list.txt > temp_user_delete_experiment.txt
				cp temp_user_delete_experiment.txt user_experiment_list.txt
				rm temp_user_delete_experiment.txt
			fi
		elif [ "$1" == 'query' -a "$2" != '' -a "$3" != '' ]
		then
			echo "Please wait while we gather your data..."
			while read line           
			do
				experiment_result=`curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$line https://mitate.cs.montana.edu/mitate_query_experiment.php`
				if [ "$experiment_result" == 'Permission denied' ]
				then
					echo "Can not retrieve data for experiment ID: $line"
				else
					echo $experiment_result >> $3
				fi
			done < $2
			echo "All the results are appended in $2 file."
		elif [ "$1" == 'init' ]
		then
			curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/mitate_initialize_db.php
		elif [ "$1" == 'update' -a "$2" != '' ]
		then
			echo "This experiment will be made public. Are you sure?(y/n))";
			read update_response;
			if [ $update_response == 'y' ]
			then
				curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_update_experiment.php
			fi
		else
			echo "Missing arguments.";
		fi
	fi
fi