<?php
$dbhostname = "localhost";
$dbusername = "mitate";
$dbpassword = "Database4Mitate";
$dbschemaname = "mitate";
$dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$dbconnection)	{die('Could not connect: ' . mysql_error());}
mysql_select_db($dbschemaname, $dbconnection);
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_POST[username]' and password = '$encrypted_password' and status = 1");
if ($loginresultset) {
    $loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == "1") {
		$experiment_count = 0;
		$device_id_array = "";
		$get_experiment_list = mysql_query("select * from experiment where experiment_id = $_POST[experiment_id] and ((username = '$_POST[username]' and permission = 'private') or permission = 'public')");
		while($get_experiment = mysql_fetch_assoc($get_experiment_list)) {
			echo "replace into experiment (experiment_id, username, permission, cellulardata, wifidata) values($get_experiment[experiment_id], '$get_experiment[username]', '$get_experiment[permission]', $get_experiment[cellulardata], $get_experiment[wifidata]);";
			$get_transaction_list = mysql_query("select * from transactions where experiment_id = $get_experiment[experiment_id]");
			while($get_transaction = mysql_fetch_assoc($get_transaction_list)) {
				echo "replace into transactions (transactionid, username, count, original_count, experiment_id) values($get_transaction[transactionid], '$get_transaction[username]', $get_transaction[count], $get_transaction[original_count], $get_transaction[experiment_id]);";
				$get_criteria_linked_list = mysql_query("select * from trans_criteria_link where transactionid = $get_transaction[transactionid]");
				while($get_criteria_linked = mysql_fetch_assoc($get_criteria_linked_list)) {
					echo "replace into trans_criteria_link (criteriaid, transactionid) values ($get_criteria_linked[criteriaid], $get_criteria_linked[transactionid]);";
					$get_criteria_list = mysql_query("select * from criteria where criteriaid = $get_criteria_linked[criteriaid]");
					while($get_criteria = mysql_fetch_assoc($get_criteria_list)) {
						echo "replace into criteria (criteriaid, specification, deviceid) values($get_criteria[criteriaid], '$get_criteria[specification]', '$get_criteria[deviceid]');";
					}
				}
				$get_transfer_linked_list = mysql_query("select * from trans_transfer_link where transactionid = $get_transaction[transactionid]");
				while($get_transfer_linked = mysql_fetch_assoc($get_transfer_linked_list)) {
					echo "replace into trans_transfer_link (transferid, transactionid, orderno) values($get_transfer_linked[transferid], $get_transfer_linked[transactionid], $get_transfer_linked[orderno]);";
					$get_transfer_list = mysql_query("select * from transfer where transferid = $get_transfer_linked[transferid]");
					while($get_transfer = mysql_fetch_assoc($get_transfer_list)) {
						echo "replace into transfer (transferid, sourceip, destinationip, bytes, type, transferadded, packetdelay, explicit, content, noofpackets, protocoltype, portnumber, contenttype, response) values ($get_transfer[transferid], '$get_transfer[sourceip]', '$get_transfer[destinationip]', $get_transfer[bytes], $get_transfer[type], '$get_transfer[transferadded]', $get_transfer[packetdelay], $get_transfer[explicit], '$get_transfer[content]', $get_transfer[noofpackets], '$get_transfer[protocoltype]', $get_transfer[portnumber], '$get_transfer[contenttype]', $get_transfer[response]);";
						$deviceid_count = 0;
						$get_metricdata_transfer_list = mysql_query("select * from metricdata where transferid = $get_transfer_linked[transferid]");
						while($get_metricdata_transfer = mysql_fetch_assoc($get_metricdata_transfer_list)) {
							$device_id_array[$deviceid_count] = $get_metricdata_transfer[deviceid];
							if($get_metricdata_transfer[value] != '') {
								echo "replace into metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid, responsedata) values ($get_metricdata_transfer[metricid], $get_metricdata_transfer[transferid], $get_metricdata_transfer[transactionid], $get_metricdata_transfer[value], '$get_metricdata_transfer[transferfinished]', '$get_metricdata_transfer[deviceid]', '$get_metricdata_transfer[responsedata]');";
							}
							else
								echo "replace into metricdata (metricid, transferid, transactionid, transferfinished, deviceid, responsedata) values ($get_metricdata_transfer[metricid], $get_metricdata_transfer[transferid], $get_metricdata_transfer[transactionid], '$get_metricdata_transfer[transferfinished]', '$get_metricdata_transfer[deviceid]', '$get_metricdata_transfer[responsedata]');";
							$deviceid_count = $deviceid_count + 1;
						}
						$get_transfermetrics_transfer_list = mysql_query("select * from transfermetrics where transferid = $get_transfer_linked[transferid]");
						while($get_transfermetrics_transfer = mysql_fetch_assoc($get_transfermetrics_transfer_list)) {
							echo "replace into transfermetrics (transferid, udppacketmetrics, tcppacketmetrics) values ($get_transfermetrics_transfer[transferid], '$get_transfermetrics_transfer[udppacketmetrics]', '$get_transfermetrics_transfer[tcppacketmetrics]');";
						}
						$get_logs_list = mysql_query("select * from logs where transferid = $get_transfer_linked[transferid]");
						while($get_log = mysql_fetch_assoc($get_logs_list)) {
							echo "replace into logs (logid, username, transferid, deviceid, logmessage) values ($get_log[logid], '$get_log[username]', $get_log[transferid], '$get_log[deviceid]', '$get_log[logmessage]', '$get_log[transferfinished]');";
						}
					}
				}
			}
			$experiment_count = $experiment_count + 1;
			$final_deviceid_array_count = count(array_unique($device_id_array));
			$final_deviceid_array = array_unique($device_id_array);
			while($final_deviceid_array_count > 0) {
				$get_deviceid_unique = $final_deviceid_array[$final_deviceid_array_count - 1];
				$get_deviceid_list = mysql_query("select DISTINCT ud.deviceid, replace(teb.devicename, '%20', ' ') as devicename, replace(teb.carriername, '%20', ' ') as devicecarrier
				from userdevice ud, transferexecutedby teb 
				where ud.deviceid = '$get_deviceid_unique'
				and ud.deviceid = teb.deviceid");
				while($get_deviceid = mysql_fetch_assoc($get_deviceid_list)) {
					echo "replace into userdevice (deviceid, devicename, devicecarrier) values ('$get_deviceid[deviceid]', '$get_deviceid[devicename]', '$get_deviceid[devicecarrier]');";
				}
				$final_deviceid_array_count = $final_deviceid_array_count - 1;
			}			
		}
		if($experiment_count == 0)
			echo "Permission denied";
	}
	else
		echo "Invalid account credentials.";
}
?>