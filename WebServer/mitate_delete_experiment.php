<?php
$con = mysql_connect("localhost","mitate","Database4Mitate");
if (!$con)
{
	die('Could not connect: ' . mysql_error());
}
mysql_select_db("mitate", $con);
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $password, MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$username' and password = '$encrypted_password'");
if ($loginresultset) {
	$loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {	
		echo "Logged in. Please wait...\n";
		$experiment_id = $_POST[experiment_id];
		if($experiment_id != "") {
			$total_credits_in_xml = mysql_fetch_assoc(mysql_query("select cellulardata, wifidata from experiment where experiment_id = $experiment_id"));
			$check_if_data_tobe_returned = mysql_query("select sum(original_count) as exp_ocount, sum(count) as exp_count from transaction1 where experiment_id = $experiment_id");
			$fetch_transaction_id_set = mysql_query("select tran.transactionid from transaction1 tran, experiment exp 
			where tran.experiment_id = $experiment_id
			and exp.experiment_id = tran.experiment_id
			and exp.username = '$username'");
			$experiment_count = 0;
			while($fetch_transaction_id_row = mysql_fetch_assoc($fetch_transaction_id_set)) {		
				$fetch_criteria_id_set = mysql_query("select criteriaid from trans_criteria_link where transactionid = $fetch_transaction_id_row[transactionid]");
				while($fetch_criteria_id_row = mysql_fetch_assoc($fetch_criteria_id_set)) {
					mysql_query("delete from criteria where criteriaid = $fetch_criteria_id_row[criteriaid]");
				}
				mysql_query("delete from trans_criteria_link where transactionid = $fetch_transaction_id_row[transactionid]");
				$fetch_transfer_id_set = mysql_query("select transferid from trans_transfer_link where transactionid = $fetch_transaction_id_row[transactionid]");
				while($fetch_transfer_id_row = mysql_fetch_assoc($fetch_transfer_id_set)) {
					mysql_query("delete from transfer where transferid = $fetch_transfer_id_row[transferid]");
					mysql_query("delete from transferexecutedby where transferid = $fetch_transfer_id_row[transferid]");
				}
				mysql_query("delete from trans_transfer_link where transactionid = $fetch_transaction_id_row[transactionid]");
				mysql_query("delete from transaction_fetched where transactionid = $fetch_transaction_id_row[transactionid]");
				mysql_query("delete from metricdata where transactionid = $fetch_transaction_id_row[transactionid]");
				mysql_query("delete from transfermetrics where transactionid = $fetch_transaction_id_row[transactionid]");
				$experiment_count = $experiment_count + 1;
			}
		if($experiment_count > 0) {
			mysql_query("delete from transaction1 where experiment_id = $experiment_id");
			mysql_query("delete from experiment where experiment_id = $experiment_id");
			Delete("user_accounts/$username/experiments/$experiment_id");
			rmdir("user_accounts/$username/$experiment_id");
			echo "Experiment deleted";
			$get_if_data_tobe_returned = mysql_fetch_assoc($check_if_data_tobe_returned);
			if($get_if_data_tobe_returned[exp_ocount] == $get_if_data_tobe_returned[exp_count]) {
				$sql="update usercredits set available_cellular_credits = (available_cellular_credits + $total_credits_in_xml[cellulardata]), available_wifi_credits = (available_wifi_credits + $total_credits_in_xml[wifidata])  where username = '$username'";
				if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
			}
		}
		else 
			echo "Valid experiment ID required.";
		}
		else
			echo "Missing experiment ID.";
	}
	else
		echo "Invalid account credentials.";
}

function Delete($path) {
    if (is_dir($path) === true) {
        $files = array_diff(scandir($path), array('.', '..'));
        foreach ($files as $file) {
            Delete(realpath($path) . '/' . $file);
        }
        return rmdir($path);
    }
    else if (is_file($path) === true) {
        return unlink($path);
    }
    return false;
}
?>