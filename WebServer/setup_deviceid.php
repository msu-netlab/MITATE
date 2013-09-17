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
			$get_deviceid_strings = mysql_query("SELECT random_string, deviceid, deviceid_long from userdevice where username = '$_GET[username]'");
			while($fetch_deviceid_strings = mysql_fetch_assoc($get_deviceid_strings))
     		{				
				if(substr(base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($_GET[phone_number]), $fetch_deviceid_strings[random_string], MCRYPT_MODE_CBC, md5(md5($_GET[phone_number])))), 0, 10) == $fetch_deviceid_strings["deviceid"]) {
					$record_found = 1;
					echo $fetch_deviceid_strings["deviceid"];
					break;
				}
			}
			if($record_found == 0) {
				$random_string = 1000000000;
				$get_random_string_counts = mysql_query("SELECT count(*) as count, max(random_string) as maxval from userdevice");
				while($fetch_random_string_count = mysql_fetch_assoc($get_random_string_counts)) {
					if($fetch_random_string_count[count] > 0)
						$random_string = $fetch_random_string_count[maxval] + 1;
				}
				$deviceid_long = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($user_phone_number), $random_string, MCRYPT_MODE_CBC, md5(md5($user_phone_number))));
				$deviceid_short = substr($deviceid_long, 0 ,10);	
				$sql_store_deviceid ="INSERT INTO userdevice (username, devicename, pollinterval, datacap, availabledata, wificap, availablewifi, deviceid, minbatterypower, random_string, deviceid_long) VALUES('$_GET[username]', '$_GET[device_name]', 30, 100, 100, 100, 100, '$deviceid_short', 50, $random_string, '$deviceid_long')";
				if (!mysql_query($sql_store_deviceid, $dbconnection)) {die('Error: ' . mysql_error());}			
				echo $deviceid_short;
			}	
		}
		else
			echo "InvalidLogin";
	}
}
?>