#!/bin/sh
isValid=0

populateUserExperimentList() {
	experimentList=`curl -k -ssl3 -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/populate_user_experiment_list.php`
	touch user_experiment_list.txt
	experimentArray=$(echo $experimentList | tr ":" "\n")
	touch user_experiment_list.txt
	for experimentId in $experimentArray
	do
		echo $experimentId >> user_experiment_list.txt
	done
}

validateExistingUserCredential() {
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
}

saveUserCredentials() {
	if [ "$ifUserIsValid" == 'true' ]
	then
		touch user.txt
		chmod 777 user.txt
		echo "$encrypted_username:$encrypted_password" > user.txt;
		chmod 444 user.txt
		isValid=1;
		if [ "$1" == 'withEcho' ]
		then
			populateUserExperimentList
			echo "You are now authenticated."
		fi
	else
		touch user.txt
		rm user.txt
		touch user_experiment_list.txt
		rm user_experiment_list.txt
	fi
}

userLogin() {
	validateExistingUserCredential
	saveUserCredentials 'withEcho'
	if [ "$ifUserIsValid" != 'true' ]
	then
		isValid=0;
		echo -n "Enter your MITATE username: ";
		read username;
		echo -n "Enter your password: ";
		read -s password;
		ifUserIsValid=`curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/validate_user.php`
		if [ "$ifUserIsValid" == 'true' ]
		then
			encrypted_username=`curl -k -ssl3 -F "string=$username" https://mitate.cs.montana.edu/encrypt_user.php`
			encrypted_password=`curl -k -ssl3 -F "string=$password" https://mitate.cs.montana.edu/encrypt_user.php`
			touch user.txt
			chmod 777 user.txt
			echo "$encrypted_username:$encrypted_password" > user.txt;
			chmod 444 user.txt
			isValid=1;
			populateUserExperimentList
			printf "\nYou are now authenticated."
		else
			isValid=0;
			printf "\nInvalid account credentials."
		fi
	fi
}

checkIfUserAlreadyLoggedIn() {
	validateExistingUserCredential
	saveUserCredentials 'withoutEcho'
}

checkIfUserAlreadyLoggedIn
if [ "$1" = "help" ]
then
	echo -e `curl -k -ssl3 https://mitate.cs.montana.edu/mitate_api_help.php`
elif [ "$1" == '' -a "$isValid" == 0 ]
then
	echo "Invalid command. To learn all possible commands, run mitate.sh help";
elif [ "$1" != '' -a "$isValid" == 0 -a "$1" != 'login' ]
then
	echo "You are not authenticated. To authenticate yourself, run mitate.sh login ";
elif [ "$1" == 'login' -a "$isValid" == 0 ]
then
	userLogin
elif [ "$1" == 'login' -a "$isValid" == 1 ]
then
	echo "You are already logged in."
elif [ "$1" == '' -a "$isValid" == 1 ]
then
	echo "Invalid arguments. To know possible commands, run mitate.sh help"
elif [ "$1" != '' -a "$isValid" == 1 ]
then
	if [ "$1" == 'upload' -a "$2" != '' ] 
	then
		echo "Please wait while we process your request..."
		result=`curl -k -ssl3 -F "username=$username" -F "password=$password" -F file=@$2 https://mitate.cs.montana.edu/mitate_upload_experiment.php`
		if [ -z "${result//[0-9]/}" ]
		then
			echo $result >> user_experiment_list.txt
			echo "Your experiment with ID: $result has been uploaded."
		else
			echo $result
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
	elif [ "$1" == 'query' -a "$2" != '' ]
	then
		echo "Please wait while we gather your data..."
		touch 'user_experiment_list.txt'
		touch $2
		while read line           
		do
			experiment_result=`curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$line https://mitate.cs.montana.edu/mitate_query_experiment.php`
			if [ "$experiment_result" == 'Permission denied' ]
			then
				echo "Can not retrieve data for experiment ID: $line"
			else
				echo $experiment_result >> $2
			fi
		done < 'user_experiment_list.txt'
		echo "The output file '$2' has been updated."
	elif [ "$1" == 'init' -a "$2" != '' ]
	then
		echo "Please wait while we generate initialization scripts..."
		echo `curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/mitate_initialize_db.php` > $2
		echo "Scripts are stored in $2 file"
	elif [ "$1" == 'makePublic' -a "$2" != '' ]
	then
		echo "This experiment will be made public. Are you sure?(y/n))";
		read update_response;
		if [ $update_response == 'y' ]
		then
			curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$2 https://mitate.cs.montana.edu/mitate_make_public_experiment.php
		fi
	elif [ "$1" == 'validate' -a "$2" != '' ]
	then
		echo `curl -k -ssl3 -F "username=$username" -F "password=$password" -F file=@$2 https://mitate.cs.montana.edu/mitate_validate_xml.php`
	elif [ "$1" == 'getExpCost' -a "$2" != '' ]
	then
		echo `curl -k -ssl3 -F "username=$username" -F "password=$password" -F file=@$2 https://mitate.cs.montana.edu/mitate_count_credit_xml.php`
	elif [ "$1" == 'checkAvailableCredits' ]
	then
		echo `curl -k -ssl3 -F "username=$username" -F "password=$password" https://mitate.cs.montana.edu/get_user_credits.php`
	elif [ "$1" == 'getExpStatus' -a "$2" != '' ]
	then
		echo -e `curl -k -ssl3 -F "username=$username" -F "password=$password" -F experiment_id=$2 https://mitate.cs.montana.edu/get_experiment_status.php`
	elif [ "$1" == 'logout' ]
	then
		touch user.txt
		touch user_experiment_list.txt
		chmod 777 user.txt
		echo "" > user.txt
		rm user.txt
		rm user_experiment_list.txt
		isValid=0
		echo "You are now logged out."
	elif [ "$1" != 'login' -a "$isValid" == 1 ]
	then
		echo "Invalid arguments. To know possible commands, run mitate.sh help";
	fi
fi