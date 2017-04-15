<?php
if ($_GET['username'] != "" && $_GET['password'] != "" && $_GET['deviceid'] != "" && $_GET['time'] != "" && $_GET['networktype'] != "" && $_GET['latitude'] != "" && $_GET['longitude'] != "" && $_GET['batterypower'] != "" && $_GET['signalstrength'] != "" && $_GET['networkcarrier'] != "" && $_GET['devicemodelname'] != "") {
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
    $encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), base64_decode($_GET[password]), MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
    $loginresultset = mysql_query("SELECT count(*) AS status FROM userinfo WHERE username = '$_GET[username]' AND password = '$encrypted_password' AND status = 1");
    if ($loginresultset) {
        $loginresultrow = mysql_fetch_assoc($loginresultset);
        if ($loginresultrow['status'] == "1") {
            $pendingtestset = mysql_query("SELECT
			trf.sourceip AS sourceip, trf.destinationip AS destinationip, trf.bytes AS bytes, trf.transferid AS transferid,
			trs.transactionid AS transactionid, trf.type AS type, trf.packetdelay, trf.explicit, substring(replace(replace(content,'\t',''), '\n\r', '\n'),1) content, trf.noofpackets, trf.portnumber, trf.contenttype, trf.response, trf.delay AS transferdelay
			FROM criteria cri, transfer trf, transactions trs, trans_criteria_link tcl, trans_transfer_link ttl, experiment exp, userdevice usrdvc
			WHERE cri.criteriaid = tcl.criteriaid
			AND trs.transactionid = tcl.transactionid
			AND trs.count > 0
			AND (find_in_set('$_GET[deviceid]', cast(REPLACE(cri.deviceid, ' ', '') as char)) > 0 OR cri.deviceid = 'client')
			AND trf.transferid = ttl.transferid
			AND ttl.transactionid = trs.transactionid
			AND trs.transactionid not in (SELECT transactionid FROM transaction_fetched WHERE deviceid = '$_GET[deviceid]')
			AND '$_GET[time]' between SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 5), ';', -1) AND SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 6), ';', -1)
			AND SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 4), ';', -1) = '$_GET[networktype]'
			AND SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 7), ';', -1) <= $_GET[batterypower]
			AND SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 8), ';', -1) <= $_GET[signalstrength]
			AND (SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 9), ';', -1) = '$_GET[networkcarrier]' OR SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 9), ';', -1) = 'allNetworkCarriers')
			AND (SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 10), ';', -1) = '$_GET[devicemodelname]' OR SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 10), ';', -1) = 'allDeviceModelNames')
			AND ((6378.137 * ACos((Cos(cast($_GET[latitude] as decimal)*(22/(180*7)))) * (Cos(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))) * (Cos((cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as decimal) - cast($_GET[longitude] as decimal))*(22/(180*7)))) + Sin(cast($_GET[latitude] as decimal)*(22/(180*7))) * (Sin(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))))) <= (cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 3), ';', -1) as decimal)*(22/(180*7))) 
			OR (6378.137 * ACos((Cos(cast($_GET[latitude] as decimal)*(22/(180*7)))) * (Cos(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))) * (Cos((cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as decimal) - cast($_GET[longitude] as decimal))*(22/(180*7)))) + Sin(cast($_GET[latitude] as decimal)*(22/(180*7))) * (Sin(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))))) = 0 )
			AND usrdvc.username = '$_GET[username]'
			AND usrdvc.deviceid = $_GET[deviceid]
			AND $_GET[batterypower] >= usrdvc.minbatterypower 
			AND exp.experiment_id = trs.experiment_id
			AND (exp.experiment_id in (
			SELECT exp1.experiment_id FROM experiment exp1
			INNER JOIN experiment exp2 on exp1.experiment_id >= exp2.experiment_id
			WHERE exp1.username != '$_GET[username]'
			GROUP BY exp1.experiment_id, exp1.wifidata
			HAVING SUM(exp2.wifidata) < (
			SELECT available_wifi_credits FROM usercredits WHERE username = '$_GET[username]'
			) AND SUM(exp2.wifidata) > 0
			UNION SELECT exp1.experiment_id
			FROM experiment exp1
			INNER JOIN experiment exp2 on exp1.experiment_id >= exp2.experiment_id
			WHERE exp1.username != '$_GET[username]'
			GROUP BY exp1.experiment_id, exp1.cellulardata
			HAVING SUM(exp2.cellulardata) < (
			SELECT available_cellular_credits FROM usercredits WHERE username = '$_GET[username]'
			) AND SUM(exp2.cellulardata) > 0
			)
			OR exp.experiment_id in (
			SELECT exp1.experiment_id FROM experiment exp1 WHERE username = '$_GET[username]'
			))
			ORDER BY trs.transactionid, ttl.orderno");
            $output = "";
            $i = 0;
            $transaction_id_array = "";
            while ($pendingtestrow = mysql_fetch_assoc($pendingtestset)) {
                $output[] = $pendingtestrow;
                if ($output[$i][contenttype] == "HEX") {
                    if (strlen($output[$i][content]) % 2 != 0)
                        $output[$i][content] = $output[$i][content] . "0";
                }
                $output_transaction_id = $output[$i][transactionid];
                $transaction_id_array[$i] = $output_transaction_id;
                $i = $i + 1;
            }
            if ($output) {
                $temp_count = count(array_unique($transaction_id_array));
                print(json_encode($output));
                $temp_check_for_null_val = 0;
                $credits_to_contribute = 0;
                $final_transaction_id_array = array_unique($transaction_id_array);
                while ($temp_count > 0 && $temp_check_for_null_val < count($transaction_id_array)) {
                    $transaction_count_reduce = $final_transaction_id_array[$temp_check_for_null_val];
                    if ($transaction_count_reduce != '') {
                        $sql_store_deviceid = "INSERT INTO transaction_fetched (transactionid, deviceid) VALUES($transaction_count_reduce, '$_GET[deviceid]')";
                        if (!mysql_query($sql_store_deviceid, $dbconnection)) {
                            die('Error: ' . mysql_error());
                        }
                        mysql_query("UPDATE transactions SET count = count - 1 WHERE transactionid = $transaction_count_reduce", $dbconnection);
                        $temp_count = $temp_count - 1;
                        $get_distinct_experiment_ids = mysql_query("SELECT distinct exp.experiment_id, exp.cellulardata, exp.wifidata 
						FROM experiment exp, transactions tran 
						WHERE tran.experiment_id = exp.experiment_id
						AND tran.experiment_id = $transaction_count_reduce
						AND exp.username != '$_GET[username]'");
                        $get_distinct_experiment_id = mysql_fetch_assoc($get_distinct_experiment_ids);
                        if ($_GET['networktype'] == "wifi")
                            $credits_to_contribute = $credits_to_contribute + $get_distinct_experiment_id[wifidata];
                        elseif ($_GET['networktype'] == "cellular")
                            $credits_to_contribute = $credits_to_contribute + $get_distinct_experiment_id[cellulardata];
                    }
                    $temp_check_for_null_val = $temp_check_for_null_val + 1;
                }
                if ($_GET['networktype'] == "wifi") {
                    mysql_query("UPDATE usercredits SET contributed_wifi_credits = contributed_wifi_credits + $credits_to_contribute WHERE username = '$_GET[username]'", $dbconnection);
                    mysql_query("UPDATE usercredits SET available_wifi_credits = available_wifi_credits - $credits_to_contribute WHERE username = '$_GET[username]'", $dbconnection);
                }
                if ($_GET['networktype'] == "cellular") {
                    mysql_query("UPDATE usercredits SET contributed_cellular_credits = contributed_cellular_credits + $credits_to_contribute WHERE username = '$_GET[username]'", $dbconnection);
                    mysql_query("UPDATE usercredits SET available_cellular_credits = available_cellular_credits - $credits_to_contribute WHERE username = '$_GET[username]'", $dbconnection);
                }
            } else {
                $pendingtestset = mysql_query("SELECT 'NoPendingTransactions' AS content");
                $pendingtestrow = mysql_fetch_assoc($pendingtestset);
                $output[] = $pendingtestrow;
                print(json_encode($output));
            }
            if ($_GET['networktype'] == "wifi")
                mysql_query("UPDATE userdevice SET timespingedwifi = timespingedwifi + 1 WHERE deviceid = $_GET[deviceid]", $dbconnection);
            if ($_GET['networktype'] == "cellular")
                mysql_query("UPDATE userdevice SET timespingedcellular = timespingedcellular + 1 WHERE deviceid = $_GET[deviceid]", $dbconnection);
        } else if ($loginresultrow['status'] == "0") {
            $pendingtestset = mysql_query("SELECT 'InvalidLogin' AS content");
            $pendingtestrow = mysql_fetch_assoc($pendingtestset);
            $output[] = $pendingtestrow;
            print(json_encode($output));
        }
    }
    mysql_close();
} else
    echo "Missing arguments";
?>