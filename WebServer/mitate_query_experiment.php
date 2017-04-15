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
        $experiment_count = 0;
        $device_id_array = "";
        $get_experiment_list = mysql_query("SELECT * FROM experiment WHERE experiment_id = $_POST[experiment_id] AND ((username = '$_POST[username]' AND permission = 'private') OR permission = 'public')");
        while ($get_experiment = mysql_fetch_assoc($get_experiment_list)) {
            echo "REPLACE INTO experiment (experiment_id, username, permission, cellulardata, wifidata) VALUES($get_experiment[experiment_id], '$get_experiment[username]', '$get_experiment[permission]', $get_experiment[cellulardata], $get_experiment[wifidata]);";
            $get_transaction_list = mysql_query("SELECT * FROM transactions WHERE experiment_id = $get_experiment[experiment_id]");
            while ($get_transaction = mysql_fetch_assoc($get_transaction_list)) {
                echo "REPLACE INTO transactions (transactionid, username, count, original_count, experiment_id) VALUES($get_transaction[transactionid], '$get_transaction[username]', $get_transaction[count], $get_transaction[original_count], $get_transaction[experiment_id]);";
                $get_criteria_linked_list = mysql_query("SELECT * FROM trans_criteria_link WHERE transactionid = $get_transaction[transactionid]");
                while ($get_criteria_linked = mysql_fetch_assoc($get_criteria_linked_list)) {
                    echo "REPLACE INTO trans_criteria_link (criteriaid, transactionid) VALUES ($get_criteria_linked[criteriaid], $get_criteria_linked[transactionid]);";
                    $get_criteria_list = mysql_query("SELECT * FROM criteria WHERE criteriaid = $get_criteria_linked[criteriaid]");
                    while ($get_criteria = mysql_fetch_assoc($get_criteria_list)) {
                        echo "REPLACE INTO criteria (criteriaid, specification, deviceid) VALUES($get_criteria[criteriaid], '$get_criteria[specification]', '$get_criteria[deviceid]');";
                    }
                }
                $get_transfer_linked_list = mysql_query("SELECT * FROM trans_transfer_link WHERE transactionid = $get_transaction[transactionid]");
                while ($get_transfer_linked = mysql_fetch_assoc($get_transfer_linked_list)) {
                    echo "REPLACE INTO trans_transfer_link (transferid, transactionid, orderno) VALUES($get_transfer_linked[transferid], $get_transfer_linked[transactionid], $get_transfer_linked[orderno]);";
                    $get_transfer_list = mysql_query("SELECT * FROM transfer WHERE transferid = $get_transfer_linked[transferid]");
                    while ($get_transfer = mysql_fetch_assoc($get_transfer_list)) {
                        echo "REPLACE INTO transfer (transferid, sourceip, destinationip, bytes, type, transferadded, packetdelay, explicit, content, noofpackets, protocoltype, portnumber, contenttype, response) VALUES ($get_transfer[transferid], '$get_transfer[sourceip]', '$get_transfer[destinationip]', $get_transfer[bytes], $get_transfer[type], '$get_transfer[transferadded]', $get_transfer[packetdelay], $get_transfer[explicit], '$get_transfer[content]', $get_transfer[noofpackets], '$get_transfer[protocoltype]', $get_transfer[portnumber], '$get_transfer[contenttype]', $get_transfer[response]);";
                        $deviceid_count = 0;
                        $get_metricdata_transfer_list = mysql_query("SELECT * FROM metricdata WHERE transferid = $get_transfer_linked[transferid]");
                        while ($get_metricdata_transfer = mysql_fetch_assoc($get_metricdata_transfer_list)) {
                            $device_id_array[$deviceid_count] = $get_metricdata_transfer[deviceid];
                            if ($get_metricdata_transfer[value] != '') {
                                echo "REPLACE INTO metricdata (metricid, transferid, transactionid, value, transferfinished, deviceid, responsedata) VALUES ($get_metricdata_transfer[metricid], $get_metricdata_transfer[transferid], $get_metricdata_transfer[transactionid], $get_metricdata_transfer[value], '$get_metricdata_transfer[transferfinished]', '$get_metricdata_transfer[deviceid]', '$get_metricdata_transfer[responsedata]');";
                            } else
                                echo "REPLACE INTO metricdata (metricid, transferid, transactionid, transferfinished, deviceid, responsedata) VALUES ($get_metricdata_transfer[metricid], $get_metricdata_transfer[transferid], $get_metricdata_transfer[transactionid], '$get_metricdata_transfer[transferfinished]', '$get_metricdata_transfer[deviceid]', '$get_metricdata_transfer[responsedata]');";
                            $deviceid_count = $deviceid_count + 1;
                        }
                        $get_transfermetrics_transfer_list = mysql_query("SELECT * FROM transfermetrics WHERE transferid = $get_transfer_linked[transferid]");
                        while ($get_transfermetrics_transfer = mysql_fetch_assoc($get_transfermetrics_transfer_list)) {
                            echo "REPLACE INTO transfermetrics (transferid, udppacketmetrics, tcppacketmetrics) VALUES ($get_transfermetrics_transfer[transferid], '$get_transfermetrics_transfer[udppacketmetrics]', '$get_transfermetrics_transfer[tcppacketmetrics]');";
                        }
                        $get_logs_list = mysql_query("SELECT * FROM logs WHERE transferid = $get_transfer_linked[transferid]");
                        while ($get_log = mysql_fetch_assoc($get_logs_list)) {
                            echo "REPLACE INTO logs (logid, username, transferid, deviceid, logmessage) VALUES ($get_log[logid], '$get_log[username]', $get_log[transferid], '$get_log[deviceid]', '$get_log[logmessage]', '$get_log[transferfinished]');";
                        }
                    }
                }
            }
            $experiment_count = $experiment_count + 1;
            $final_deviceid_array_count = count(array_unique($device_id_array));
            $final_deviceid_array = array_unique($device_id_array);
            while ($final_deviceid_array_count > 0) {
                $get_deviceid_unique = $final_deviceid_array[$final_deviceid_array_count - 1];
                $get_deviceid_list = mysql_query("SELECT DISTINCT ud.deviceid, replace(teb.devicename, '%20', ' ') AS devicename, replace(teb.carriername, '%20', ' ') AS devicecarrier
				FROM userdevice ud, transferexecutedby teb 
				WHERE ud.deviceid = '$get_deviceid_unique'
				AND ud.deviceid = teb.deviceid");
                while ($get_deviceid = mysql_fetch_assoc($get_deviceid_list)) {
                    echo "REPLACE INTO userdevice (deviceid, devicename, devicecarrier) VALUES ('$get_deviceid[deviceid]', '$get_deviceid[devicename]', '$get_deviceid[devicecarrier]');";
                }
                $final_deviceid_array_count = $final_deviceid_array_count - 1;
            }
        }
        if ($experiment_count == 0)
            echo "Permission denied";
    } else
        echo "Invalid account credentials.";
}
?>