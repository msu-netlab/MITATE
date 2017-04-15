<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$passwordEncryptionKey = $xml->database->passwordEncryptionKey;
$dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$dbconnection) {
    die('Could not connect: ' . mysql_error());
}
mysql_select_db($dbschemaname, $dbconnection);
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $_POST[password], MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
$loginresultset = mysql_query("SELECT count(*) AS status FROM userinfo WHERE username = '$_POST[username]' AND password = '$encrypted_password' AND status = 1");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
        $get_status_list = mysql_query("SELECT count(teb.transferid) AS totalExecuted, count(ttl.transferid) AS totalTransfers
		FROM experiment exp, transactions tr, trans_transfer_link ttl 
		LEFT OUTER JOIN transferexecutedby teb 
		ON ttl.transferid = teb.transferid
		WHERE exp.experiment_id = $_POST[experiment_id]
		AND exp.username = '$_POST[username]'
		AND exp.experiment_id = tr.experiment_id
		AND tr.transactionid = ttl.transactionid;");
        while ($get_status = mysql_fetch_assoc($get_status_list)) {
            if ($get_status[totalTransfers] != 0)
                echo 'Total number of transfer(s): ' . $get_status[totalTransfers] . '\nTotal number of transfer(s) executed: ' . $get_status[totalExecuted] . '\nTo get more details, run mitate.sh query <outfile>';
            else
                echo 'You do not have permissions to get the status of this experiment.';
        }
    }
}