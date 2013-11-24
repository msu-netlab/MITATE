<?php
$dbhostname = "localhost";
$dbusername = "mitate";
$dbpassword = "Database4Mitate";
$dbschemaname = "mitate";
$dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$dbconnection)	{die('Could not connect: ' . mysql_error());}
mysql_select_db($dbschemaname, $dbconnection);
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_POST[username]' and password = '$encrypted_password'");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
		$db_create_table_query_code = "CREATE TABLE IF NOT EXISTS `criteria` ( `criteriaid` int(10), `specification` varchar(500), `deviceid` varchar(200) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `experiment` ( `experiment_id` int(13), `username` varchar(20), `permission` varchar(7), `cellulardata` decimal(10,2), `wifidata` decimal(10,2) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metric` ( `name` varchar(100), `metricid` int(6) ) ENGINE=MyISAM AUTO_INCREMENT=10039 DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `metricdata` ( `metricid` int(5), `transferid` int(10), `transactionid` int(10), `value` decimal(18,10), `transferfinished` varchar(50), `deviceid` varchar(200), `responsedata` varchar(512) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_criteria_link` ( `criteriaid` int(10), `transactionid` int(20) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `trans_transfer_link` ( `transferid` int(10), `transactionid` int(10), `orderno` int(2) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transaction1` ( `transactionid` int(10), `username` varchar(20), `count` int(3), `original_count` int(3), `experiment_id` int(13) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transaction_fetched` ( `transactionid` int(10), `deviceid` varchar(15) ) ENGINE=InnoDB DEFAULT CHARSET=utf8; CREATE TABLE IF NOT EXISTS `transfer` ( `transferid` int(10), `sourceip` varchar(100), `destinationip` varchar(100), `bytes` int(10), `type` int(1), `transferadded` varchar(50), `packetdelay` int(10), `explicit` int(1), `content` mediumtext, `noofpackets` int(4), `protocoltype` varchar(10), `portnumber` int(5), `contenttype` varchar(10), `response` int(1) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transferexecutedby` ( `transferid` int(10), `devicename` varchar(50), `username` varchar(20), `carriername` varchar(50), `deviceid` varchar(15) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `transfermetrics` ( `transferid` int(10), `transactionid` int(10), `udppacketmetrics` blob, `tcppacketmetrics` blob, `udplatencyconf` decimal(10,2), `udpthroughputconf` decimal(10,2), `tcplatencyconf` decimal(10,2) , `tcpthroughputconf` decimal(10,2), `deviceid` varchar(15) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `userdevice` ( `devicename` varchar(50), `deviceid` varchar(10) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `userinfo` ( `fname` varchar(20), `lname` varchar(20), `username` varchar(20), `email` varchar(50), `datecreated` date ) ENGINE=MyISAM DEFAULT CHARSET=latin1;CREATE TABLE `usercredits` ( `credit_id` int(10) NOT NULL, `username` varchar(20) DEFAULT NULL, `available_cellular_credits` decimal(10,2) DEFAULT '0.00', `contributed_cellular_credits` decimal(10,2) DEFAULT '0.00', `available_wifi_credits` decimal(10,2) DEFAULT '0.00', `contributed_wifi_credits` decimal(10,2) DEFAULT '0.00', PRIMARY KEY (`credit_id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `logs` (`logid` int(5), `username` varchar(20), `transferid` int(10), `deviceid` varchar(10), `logmessage` mediumtext, `transferfinished` varchar(50)) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		echo $db_create_table_query_code;
		$get_user_list = mysql_query("select * from userinfo where username = '$_POST[username]'");
		while($get_user = mysql_fetch_assoc($get_user_list)) {
			echo "replace into userinfo (fname, lname, username, email, datecreated) values ('$get_user[fname]', '$get_user[lname]', '$get_user[username]', '$get_user[email]', '$get_user[datecreated]');";
		}
		$get_user_credits = mysql_query("select * from usercredits where username = '$_POST[username]'");
		while($get_user_credit = mysql_fetch_assoc($get_user_credits)) {
			echo "replace into usercredits (credit_id, username, available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits) values ($get_user_credit[credit_id], '$get_user_credit[username]', $get_user_credit[available_cellular_credits], $get_user_credit[contributed_cellular_credits], $get_user_credit[available_wifi_credits], $get_user_credit[contributed_wifi_credits]);";
		}
		$get_metric_list = mysql_query("select * from metric");
		while($get_metric = mysql_fetch_assoc($get_metric_list)) {
			echo "replace into metric (name, metricid) values ('$get_metric[name]', $get_metric[metricid]);";
		}
	}
	else
		echo "Invalid account credentials.";
}
?>