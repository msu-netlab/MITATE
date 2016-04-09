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
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_POST[username]' and password = '$encrypted_password' and status = 1");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
		$get_status_list = mysql_query("select count(teb.transferid) as totalExecuted, count(ttl.transferid) as totalTransfers
		from experiment exp, transactions tr, trans_transfer_link ttl left outer join transferexecutedby teb on ttl.transferid = teb.transferid
		where exp.experiment_id = $_POST[experiment_id]
		and exp.username = '$_POST[username]'
		and exp.experiment_id = tr.experiment_id
		and tr.transactionid = ttl.transactionid;");
		while($get_status = mysql_fetch_assoc($get_status_list)) {
			if($get_status[totalTransfers] != 0)
				echo 'Total number of transfer(s): ' . $get_status[totalTransfers] . '\nTotal number of transfer(s) executed: ' .  $get_status[totalExecuted] . '\nTo get more details, run mitate.sh query <outfile>';
			else
				echo 'You do not have permissions to get the status of this experiment.';
		}
	}
}