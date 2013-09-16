<?php
$con = mysql_connect("localhost","mitate","Database4Mitate");
if (!$con)
{
	die('Could not connect: ' . mysql_error());
}
mysql_select_db("mitate", $con);
$username = $_POST[username];
$password = $_POST[password];
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$username' and password = '$password'");
if ($loginresultset) {
	$loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {	
		echo "Logged in. Please wait...\n";
		$experiment_id = $_POST[experiment_id];
		if($experiment_id != "") {
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
			$files_to_delete = glob('user_accounts/' . $username . '/' . $experiment_id . '/*');
			foreach($files_to_delete as $file_to_delete){
				if(is_file($file_to_delete))
					unlink($file_to_delete);
			}
			rmdir("user_accounts/$username/$experiment_id");
			echo "Experiment deleted";
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
?>