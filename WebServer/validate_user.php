<?php
libxml_use_internal_errors(true); 
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$passwordEncryptionKey = $xml->database->passwordEncryptionKey;
$webServerAddress = $xml->webServer->address;
$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$con)
{
	die('Could not connect: ' . mysql_error());
}
mysql_select_db("mitate", $con);
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $password, MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$username' and password = '$encrypted_password' and status = 1");
if ($loginresultset) {
	$loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {
		echo "true";
	}
	else
		echo "false";
}
?>