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
        $get_experiment_list = mysql_query("SELECT experiment_id FROM experiment WHERE username = '$_POST[username]';");
        $experiment_list = "";
        while ($get_experiment = mysql_fetch_assoc($get_experiment_list)) {
            echo $get_experiment[experiment_id] . ":";
        }
    }
}