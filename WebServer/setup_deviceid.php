<?php
if($_GET['username']!="" && $_GET['password']!="" && $_GET['phone_number']!="" && $_GET['device_name']!="") {
	$dbhostname = "localhost";
	$dbusername = "mitate";
    $dbpassword = "Database4Mitate";
    $dbschemaname = "mitate";
    $dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
    if (!$dbconnection)	{
        die('Could not connect: ' . mysql_error());
    }
    mysql_select_db($dbschemaname, $dbconnection);
	$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_GET[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
	$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_GET[username]' and password = '$encrypted_password'");
    if ($loginresultset) {
		$loginresultrow = mysql_fetch_assoc($loginresultset);
        if ($loginresultrow['status'] == "1") {		
			$record_found = 0;
			$user_phone_number = $_GET[phone_number];
			$deviceid_from_database = "";
			$encrypted_user_phone_number = md5($user_phone_number);
			$get_deviceid_strings = mysql_query("SELECT deviceid from userdevice where username = '$_GET[username]' and random_String = '$encrypted_user_phone_number'");
			while($fetch_deviceid_strings = mysql_fetch_assoc($get_deviceid_strings))
     		{							
					$record_found = 1;
					echo $fetch_deviceid_strings["deviceid"];
			}
			if($record_found == 0) {
				$random_string = 1000000000;
				$get_random_string_counts = mysql_query("SELECT count(*) as count, max(deviceid) as maxval from userdevice");
				while($fetch_random_string_count = mysql_fetch_assoc($get_random_string_counts)) {
					if($fetch_random_string_count[count] > 0)
						$random_string = $fetch_random_string_count[maxval] + 1;
				}
				$deviceid_short = md5($user_phone_number);	
				$sql_store_deviceid ="INSERT INTO userdevice (username, devicename, pollinterval, datacap, availabledata, wificap, availablewifi, deviceid, minbatterypower, random_string) VALUES('$_GET[username]', '$_GET[device_name]', 30, 100, 100, 100, 100, $random_string, 50, '$deviceid_short')";
				if (!mysql_query($sql_store_deviceid, $dbconnection)) {die('Error: ' . mysql_error());}			
				echo $random_string;
			}	
		}
		else
			echo "InvalidLogin";
	}
}
?>