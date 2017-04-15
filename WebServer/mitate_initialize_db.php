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
        $db_create_table_query_code = "CREATE TABLE IF NOT EXISTS `criteria` ( `criteriaid` INT(10) PRIMARY KEY, `specification` VARCHAR(500), `deviceid` VARCHAR(200) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `experiment` ( `experiment_id` INT(10) PRIMARY KEY, `username` VARCHAR(20), `permission` VARCHAR(7), `cellulardata` DECIMAL(10,2), `wifidata` DECIMAL(10,2) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metric` ( `name` VARCHAR(100), `metricid` INT(6) PRIMARY KEY) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metricdata` ( `metricid` INT(5), `transferid` INT(10), `transactionid` INT(10), `value` DECIMAL(18,10), `transferfinished` VARCHAR(50), `deviceid` VARCHAR(200), `responsedata` VARCHAR(512), UNIQUE KEY `mttdt` (`metricid`,`transferid`,`transactionid`,`deviceid`,`transferfinished`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_criteria_link` ( `criteriaid` INT(10), `transactionid` INT(20), PRIMARY KEY (`criteriaid`, `transactionid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_transfer_link` ( `transferid` INT(10), `transactionid` INT(10), `orderno` INT(2), PRIMARY KEY (`transactionid`, `transferid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transactions` ( `transactionid` INT(10) PRIMARY KEY, `username` VARCHAR(20), `count` INT(3), `original_count` INT(3), `experiment_id` INT(13) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transfer` ( `transferid` INT(10) PRIMARY KEY, `sourceip` VARCHAR(100), `destinationip` VARCHAR(100), `bytes` INT(10), `type` INT(1), `transferadded` VARCHAR(50), `packetdelay` INT(10), `explicit` INT(1), `content` MEDIUMTEXT, `noofpackets` INT(4), `protocoltype` VARCHAR(10), `portnumber` INT(5), `contenttype` VARCHAR(10), `response` INT(1) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transfermetrics` ( `transferid` INT(10) PRIMARY KEY, `udppacketmetrics` LONGTEXT, `tcppacketmetrics` LONGTEXT ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `userdevice` ( `deviceid` VARCHAR(10) PRIMARY KEY, `devicename` VARCHAR(50), `devicecarrier` VARCHAR(50) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `logs` (`logid` INT(5) PRIMARY KEY, `username` VARCHAR(20), `transferid` INT(10), `deviceid` VARCHAR(10), `logmessage` MEDIUMTEXT, `transferfinished` VARCHAR(50)) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE `packetmetrics` ( `transferid` INT(10), `packetid` INT(10), `throughput` DECIMAL(10,2), `latency` DECIMAL(10,2), PRIMARY KEY(`transferid`, `packetid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
        echo $db_create_table_query_code;
        $get_metric_list = mysql_query("SELECT * FROM metric");
        while ($get_metric = mysql_fetch_assoc($get_metric_list)) {
            echo "REPLACE INTO metric (name, metricid) VALUES ('$get_metric[name]', $get_metric[metricid]);";
        }
    } else
        echo "Invalid account credentials.";
}
?>