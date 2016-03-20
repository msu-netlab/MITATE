#!/bin/sh
isValid=0

populateUserExperimentList() {
	experimentList=$(curl   -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/populate_user_experiment_list.php)
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
		username=$(curl   -F "string=$encrypted_username" http://mitate.cs.montana.edu/decrypt_user.php)
		password=$(curl   -F "string=$encrypted_password" http://mitate.cs.montana.edu/decrypt_user.php)
	else
		username=$encrypted_username
		password=$encrypted_password
	fi
	ifUserIsValid=$(curl   -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/validate_user.php)
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
		ifUserIsValid=$(curl   -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/validate_user.php)
		if [ "$ifUserIsValid" == 'true' ]
		then
			encrypted_username=$(curl   -F "string=$username" http://mitate.cs.montana.edu/encrypt_user.php)
			encrypted_password=$(curl   -F "string=$password" http://mitate.cs.montana.edu/encrypt_user.php)
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

invalidArgs() {
	echo "Invalid arguments. Login to continue or to know possible commands, run mitate.sh help"
}

checkIfUserAlreadyLoggedIn() {
	validateExistingUserCredential
	saveUserCredentials 'withoutEcho'
}

checkIfUserAlreadyLoggedIn

if [ "$1" == '' ]
then
	invalidArgs
elif [ "$2" == '' ]
then
	if [ "$1" == "help" ]
	then
		echo -e $(curl http://mitate.cs.montana.edu/mitate_api_help.php)
	elif [ "$1" == 'login' -a "$isValid" == 0 ]
	then
		userLogin
	elif [ "$1" == 'login' -a "$isValid" == 1 ]
	then
		echo "You are already logged in."
	elif [ "$1" == 'logout' ]
        then
		if [ "$isValid" == 1 ]
		then
	                touch user.txt
        	        touch user_experiment_list.txt
                	chmod 777 user.txt
                	echo "" > user.txt
                	rm user.txt
                	rm user_experiment_list.txt
                	isValid=0
                	echo "You are now logged out."
		else
			echo "You are not logged in."
		fi
	elif [ "$1" == 'checkAvailableCredits' ]
	then
		if [ "$isValid" == 0 ]
		then
			echo "You are not logged in. Please login to continue."
		else
			echo $(curl -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/get_user_credits.php)
		fi
	else
		invalidArgs
	fi
elif [ "$2" != '' ] 
then
	if [ "$isValid" == 0 ]
        then
                if [ "$2" != 'getExpStatus' -o "$2" != 'getExpCost' -o "$2" != 'validate' -o "$2" != 'makePublic' -o "$2" != 'init' -o "$2" != 'query' -o "$2" != 'delete' -o "$2" != 'upload' ]
                then
                        invalidArgs
        	else
                	echo "You are not logged in. Pleaes login to continue."
        	fi
	elif [ "$isValid" == 1 ]
	then
		if [ "$1" == 'upload' ] 
		then
			echo "Please wait while we process your request..."
			if [ -e "$2" ] 
			then
				result=$(curl -F "username=$username" -F "password=$password" -F file=@$2 http://mitate.cs.montana.edu/mitate_upload_experiment.php)
                		if [ -z "${result//[0-9]/}" ]
                		then
                        		echo $result >> user_experiment_list.txt
                        		echo "Your experiment with ID: $result has been uploaded."
                		else
                        		echo $result
                		fi

			else
				echo "File does not exists. Please check the filename and try again."
			fi
		elif [ "$1" == 'delete' ] 
		then
			echo "This experiment will be deleted. Are you sure?(y/n)";
			read delete_response;
			if [ $delete_response == 'y' ]
			then
				curl -F "username=$username" -F "password=$password" -F experiment_id=$2 http://mitate.cs.montana.edu/mitate_delete_experiment.php
				touch temp_user_delete_experiment.txt
				grep -v $2 user_experiment_list.txt > temp_user_delete_experiment.txt
				cp temp_user_delete_experiment.txt user_experiment_list.txt
				rm temp_user_delete_experiment.txt
			fi
		elif [ "$1" == 'query' ]
		then
			echo "Please wait while we gather your data..."
			touch 'user_experiment_list.txt'
			touch $2
			while read line           
			do
				experiment_result=$(curl -F "username=$username" -F "password=$password" -F experiment_id=$line http://mitate.cs.montana.edu/mitate_query_experiment.php)
				if [ "$experiment_result" == 'Permission denied' ]
				then
					echo "Can not retrieve data for experiment ID: $line"
				else
					echo $experiment_result >> $2
				fi
			done < 'user_experiment_list.txt'
			echo "The output file '$2' has been replaced."
		elif [ "$1" == 'init' ]
		then
			echo "Please wait while we generate initialization scripts..."
			echo $(curl -F "username=$username" -F "password=$password" http://mitate.cs.montana.edu/mitate_initialize_db.php) > $2
			echo "The database initialization scripts are now saved in $2 file"
		elif [ "$1" == 'makePublic' ]
		then
			echo "This experiment will be made public. Are you sure?(y/n))";
			read update_response;
			if [ $update_response == 'y' ]
			then
				curl -F "username=$username" -F "password=$password" -F experiment_id=$2 http://mitate.cs.montana.edu/mitate_make_public_experiment.php
			fi
		elif [ "$1" == 'validate' ]
		then
			if [ -e "$2" ]
                	then
				echo $(curl -F "username=$username" -F "password=$password" -F file=@$2 http://mitate.cs.montana.edu/mitate_validate_xml.php)
			else
                        	echo "File does not exists. Please check the filename and try again."
                	fi
		elif [ "$1" == 'getExpCost' ]
		then
			if [ -e "$2" ]
                	then
				echo $(curl -F "username=$username" -F "password=$password" -F file=@$2 http://mitate.cs.montana.edu/mitate_count_credit_xml.php)
			else
                       		echo "File does not exists. Please check the filename and try again."
                	fi
		elif [ "$1" == 'getExpStatus' ]
		then
			echo -e $(curl -F "username=$username" -F "password=$password" -F experiment_id=$2 http://mitate.cs.montana.edu/get_experiment_status.php)
		else
			invalidArgs
		fi
	fi
fi
