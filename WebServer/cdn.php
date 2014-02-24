<?php
    $con = mysql_connect("localhost","mitate","Database4Mitate");
	if (!$con)
	{
		die('Could not connect: ' . mysql_error());
	}
	mysql_select_db("mitate", $con);
	$k = 0;
	$time = str_replace("T", " ", $_POST[time]);
	if($_POST[oneway] == 0 && $_POST[size] == 0 && $_POST[rtt] == 0) {
		$sql="insert into logs (username, transferid, deviceid, logmessage, transferfinished) values('$_POST[username]', $_POST[transferid], '$_POST[deviceid]', '$_POST[log]', '$_POST[time]')";
		if (!mysql_query($sql,$con)) {
			die('Error: ' . mysql_error());
		}
	}
	else {
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10027, $_POST[transferid], $_POST[transactionid], $_POST[oneway], '$time', '$_POST[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10028, $_POST[transferid], $_POST[transactionid], $_POST[rtt], '$time', '$_POST[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$onewaythrouhput = $_POST[size]/$_POST[oneway];
	$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid) VALUES(10029, $_POST[transferid], $_POST[transactionid], $onewaythrouhput, '$time', '$_POST[deviceid]')";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	

	$sql="update metricdata set transferfinished = '$time' where transferid = $_POST[transferid] and transactionid = $_POST[transactionid] and deviceid = '$_POST[deviceid]'";
	if (!mysql_query($sql,$con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	$sql="insert into transferexecutedby values($_POST[transferid], '$_POST[devicename]', '$_POST[username]', '$_POST[mobilecarrier]', '$_POST[deviceid]')";
	if (!mysql_query($sql, $con))
	{
	die('Error: ' . mysql_error());
	}
	else $k = $k +1;
	
	if($k==5) {
		$sql="INSERT INTO metricdata (metricid, transferid, transactionid, transferfinished, deviceid, responsedata) VALUES(10040, $_POST[transferid], $_POST[transactionid], '$time', '$_POST[deviceid]', '$_POST[log]')";
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