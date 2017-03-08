<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
list($bigQuery, $dataset) = require 'get_bq_connection.php';
if (!$bigQuery) {
    die('Could not connect to BigQuery');
}
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $_POST[password], MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
$loginresultset = $bigQuery->runQuery("SELECT COUNT(*) AS status FROM MITATE.userinfo WHERE username = '$_POST[username]' AND password = '$encrypted_password' AND status = 1");
$queryInfo = $loginresultset->info();
if ($info['totalRows'] > 0) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
        $get_status_list = $bigQuery->runQuery("SELECT COUNT(teb.transferid) AS totalExecuted, COUNT(ttl.transferid) AS totalTransfers
		FROM MITATE.experiment exp, MITATE.transactions tr, MITATE.trans_transfer_link ttl LEFT OUTER JOIN MITATE.transferexecutedby teb ON ttl.transferid = teb.transferid
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