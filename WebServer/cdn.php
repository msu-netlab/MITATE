<?php
    $con = mysql_connect("localhost","mitate","Database4Mitate");
	if (!$con)
	{
		die('Could not connect: ' . mysql_error());
	}
	mysql_select_db("mitate", $con);
	$k = 0;
	$time = str_replace("T", " ", $_GET[time]);
	if($_GET[oneway] == 0 && $_GET[size] == 0 && $_GET[rtt] == 0) {
		$sql="insert into logs (username, transferid, deviceid, logmessage) values('$_GET[username]', $_GET[transferid], '$_GET[deviceid]', '$_GET[log]')";
		if (!mysql_query($sql,$con)) {
			die('Error: ' . mysql_error());
		}
	}
	else {
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10027, $_GET[transferid], $_GET[transactionid], $_GET[oneway], '$time', '$_GET[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10028, $_GET[transferid], $_GET[transactionid], $_GET[rtt], '$time', '$_GET[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$onewaythrouhput = $_GET[size]/$_GET[oneway];
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10029, $_GET[transferid], $_GET[transactionid], $onewaythrouhput, '$time', '$_GET[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	

	$sql="update metricdata set transferfinished = '$time' where transferid = $_GET[transferid] and transactionid = $_GET[transactionid] and deviceid = '$_GET[deviceid]'";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$sql="insert into transferexecutedby values($_GET[transferid], '$_GET[devicename]', '$_GET[username]', '$_GET[mobilecarrier]', '$_GET[deviceid]')";
	if (!mysql_query($sql, $con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	if($k==5) {
		$sql="INSERT INTO metricdata (metricid, transferid, transactionid, transferfinished, deviceid, responsedata) VALUES(10040, $_GET[transferid], $_GET[transactionid], '$time', '$_GET[deviceid]', '$_GET[log]')";
		if (!mysql_query($sql, $con)) {
			die('Error: ' . mysql_error());
		}
		echo "1";
	}
	else
		echo "0";
	}
	mysqli_close($con);

?>