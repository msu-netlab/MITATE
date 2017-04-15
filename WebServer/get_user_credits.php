<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$passwordEncryptionKey = $xml->database->passwordEncryptionKey;
$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$con) {
    die('Could not connect: ' . mysql_error());
}
mysql_select_db($dbschemaname, $con);
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $password, MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
$loginresultset = mysql_query("SELECT count(*) AS status FROM userinfo WHERE username = '$username' AND password = '$encrypted_password' AND status = 1");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {
        $check_user_credits = mysql_query("SELECT available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits FROM usercredits WHERE username = '$username'");
        $check_user_credit = mysql_fetch_assoc($check_user_credits);
        echo 'Total available cellular credits: ' . $check_user_credit[available_cellular_credits] . ' MB\n Total contributed cellular credits: ' . $check_user_credit[contributed_cellular_credits] . ' MB\n Total available Wi-Fi credits: ' . $check_user_credit[available_wifi_credits] . ' MB\n Total contributed Wi-Fi credits: ' . $check_user_credit[contributed_wifi_credits] . ' MB';
    } else
        echo "Invalid login";
}
?>