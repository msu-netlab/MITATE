<?php
$dbhostname = "localhost";
$dbusername = "mitate";
$dbpassword = "Database4Mitate";
$dbschemaname = "mitate";
$dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$dbconnection)	{die('Could not connect: ' . mysql_error());}
mysql_select_db($dbschemaname, $dbconnection);
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_POST[username]' and password = '$encrypted_password'  and status = 1");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
		$db_create_table_query_code = "CREATE TABLE IF NOT EXISTS `criteria` ( `criteriaid` int(10) PRIMARY KEY, `specification` varchar(500), `deviceid` varchar(200) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `experiment` ( `experiment_id` int(10) PRIMARY KEY, `username` varchar(20), `permission` varchar(7), `cellulardata` decimal(10,2), `wifidata` decimal(10,2) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metric` ( `name` varchar(100), `metricid` int(6) PRIMARY KEY) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metricdata` ( `metricid` int(5), `transferid` int(10), `transactionid` int(10), `value` decimal(18,10), `transferfinished` varchar(50), `deviceid` varchar(200), `responsedata` varchar(512), UNIQUE KEY `mttdt` (`metricid`,`transferid`,`transactionid`,`deviceid`,`transferfinished`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_criteria_link` ( `criteriaid` int(10), `transactionid` int(20), PRIMARY KEY (`criteriaid`, `transactionid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_transfer_link` ( `transferid` int(10), `transactionid` int(10), `orderno` int(2), PRIMARY KEY (`transactionid`, `transferid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transactions` ( `transactionid` int(10) PRIMARY KEY, `username` varchar(20), `count` int(3), `original_count` int(3), `experiment_id` int(13) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transfer` ( `transferid` int(10) PRIMARY KEY, `sourceip` varchar(100), `destinationip` varchar(100), `bytes` int(10), `type` int(1), `transferadded` varchar(50), `packetdelay` int(10), `explicit` int(1), `content` mediumtext, `noofpackets` int(4), `protocoltype` varchar(10), `portnumber` int(5), `contenttype` varchar(10), `response` int(1) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transfermetrics` ( `transferid` int(10) PRIMARY KEY, `udppacketmetrics` longtext, `tcppacketmetrics` longtext ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `userdevice` ( `deviceid` varchar(10) PRIMARY KEY, `devicename` varchar(50), `devicecarrier` varchar(50) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `logs` (`logid` int(5) PRIMARY KEY, `username` varchar(20), `transferid` int(10), `deviceid` varchar(10), `logmessage` mediumtext, `transferfinished` varchar(50)) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE `packetmetrics` ( `transferid` int(10), `packetid` int(10), `throughput` decimal(10,2), `latency` decimal(10,2), PRIMARY KEY(`transferid`, `packetid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		echo $db_create_table_query_code;
		$get_metric_list = mysql_query("select * from metric");
		while($get_metric = mysql_fetch_assoc($get_metric_list)) {
			echo "replace into metric (name, metricid) values ('$get_metric[name]', $get_metric[metricid]);";
		}
	}
	else
		echo "Invalid account credentials.";
}
?>