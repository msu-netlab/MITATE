<?php include('header.php'); ?>
<br />
<?php
libxml_use_internal_errors(true); 
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$dbconnection)	{die('Could not connect: ' . mysql_error());}
mysql_select_db($dbschemaname, $dbconnection);
$verificationresult = mysql_query("SELECT * FROM user_verification where verification_key = '$_GET[key]' limit 1");
$num_records = 0;
while($verificationresultrow = mysql_fetch_assoc($verificationresult)) {
	$num_records = $num_records + 1;
	if($verificationresultrow[if_used] == 1)
		echo "<h2>You are already registered.";
	else {
		mysql_query("update user_verification set if_used = 1 where verification_key = '$_GET[key]'", $dbconnection);
		mysql_query("update userinfo set status = 1 where username = '$verificationresultrow[username]'", $dbconnection);
		$start_value = 1000000000;
		$credit_id = $start_value;
		$get_credit_id_counts = mysql_query("SELECT count(*) as count, max(credit_id) as maxval from usercredits");
		while($get_credit_id_count = mysql_fetch_assoc($get_credit_id_counts)) {
			if($get_credit_id_count[count] > 0)
				$credit_id = $get_credit_id_count[maxval] + 1;
		}
		$sql_store_credits ="INSERT INTO usercredits (credit_id, username, available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits) VALUES($credit_id, '$verificationresultrow[username]', 200, 0, 500, 0)";
		if (!mysql_query($sql_store_credits, $dbconnection)) {die('Error: ' . mysql_error());}	
		echo "<h2>You are now registered with MITATE.";
	}
	echo " Please refer to our tutorial to proceed.</h2>";
}
if($num_records == 0)
	echo "<h2>Invalid URL.</h2>"
?>
<?php include('footer.php'); ?>