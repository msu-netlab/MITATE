<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
list($bigQuery, $dataset) = require 'get_bq_connection.php';
if (!$bigQuery) {
    die('Could not connect to BigQuery');
}
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $password, MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
$loginresultset = $bigQuery->runQuery("SELECT COUNT(*) AS status FROM MITATE.userinfo WHERE username = '$username' AND password = '$encrypted_password' AND status = 1");
$queryInfo = $loginresultset->info();
if ($queryInfo['totalRows'] > 0) {
    foreach ($loginresultset->rows() as $loginresultrow) {
        if ($loginresultrow['status'] == "1") {
            $check_user_credits = $bigQuery->runQuery("SELECT available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits 
                                                       FROM MITATE.usercredits 
                                                       WHERE username = '$username'");
            foreach ($check_user_credits->rows() as $check_user_credit) {
                echo 'Total available cellular credits: ' . $check_user_credit['available_cellular_credits'] . ' MB\n Total contributed cellular credits: ' . $check_user_credit['contributed_cellular_credits'] . ' MB\n Total available Wi-Fi credits: ' . $check_user_credit['available_wifi_credits'] . ' MB\n Total contributed Wi-Fi credits: ' . $check_user_credit['contributed_wifi_credits'] . ' MB';
            }
        } else
            echo "Invalid login";
    }
}
?>