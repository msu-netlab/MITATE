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
        echo "Logged in. Please wait...\n";
        $experiment_id = $_POST[experiment_id];
        if ($experiment_id != "") {
            $count = 0;
            $get_experiment_list = mysql_query("SELECT * FROM experiment WHERE username = '$username' AND experiment_id = $experiment_id");
            while ($get_experiment = mysql_fetch_assoc($get_experiment_list)) {
                $count = $count + 1;
            }
            if ($count > 0) {
                mysql_query("UPDATE experiment SET permission = 'public' WHERE experiment_id = $experiment_id", $con);
                echo "The experiment is now public.";
            } else
                echo "Permission denied.";
        } else
            echo "Missing experiment ID.";
    } else
        echo "Invalid account credentials.";
}
?>
