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
			$start_value = 1000000000;
			$experiment_id = $start_value;
			$get_experiment_id_counts = mysql_query("SELECT count(*) as count, max(experiment_id) as maxval from experiment");
			while($get_experiment_id_count = mysql_fetch_assoc($get_experiment_id_counts)) {
				if($get_experiment_id_count[count] > 0)
					$experiment_id = $get_experiment_id_count[maxval] + 1;
			}
			$yesdone = 0;
			if($_FILES["file"]["name"] != "") {
				if ($_FILES["file"]["error"] > 0) {
					echo "Error: " . $_FILES["file"]["error"] . "<br />";
				}
				else {
					$file_extension = end(explode(".", $_FILES["file"]["name"]));
					$file_name_without_extension = basename($_FILES["file"]["name"], ".xml");
					$final_file_path = $file_name_without_extension . $experiment_id . "." . $file_extension;
					mkdir("user_accounts/$username/$experiment_id", 0777);
					move_uploaded_file($_FILES["file"]["tmp_name"],"user_accounts/$username/$experiment_id/" . $final_file_path);		
					$yesdone = 1;
				}
			}
			else echo "Error: File not specified";
			$filepath = "user_accounts/" . $username . "/$experiment_id/" . $final_file_path;
			$xml = simplexml_load_file("$filepath");
			$sql="INSERT INTO experiment (experiment_id, username, permission) VALUES($experiment_id, '$username', 'private')";
			if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
			foreach($xml->transactions->transaction as $temptransaction) {
				$order=1;
				$transactionid = $start_value;
				$get_transactionid_counts = mysql_query("SELECT count(*) as count, max(transactionid) as maxval from transaction1");
				while($get_transactionid_count = mysql_fetch_assoc($get_transactionid_counts)) {
					if($get_transactionid_count[count] > 0)
						$transactionid = $get_transactionid_count[maxval] + 1;
				}
				$transaction_count = $temptransaction["count"];
				if($transaction_count != "") { 
					$sql="INSERT INTO transaction1 (transactionid, username, count, original_count, experiment_id) VALUES($transactionid, '$username', $transaction_count, $transaction_count, $experiment_id)";
					if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
				}
				if($transaction_count == ""){
					$sql="INSERT INTO transaction1 (transactionid, username, experiment_id) VALUES($transactionid,'$username', $experiment_id)";
					if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
				}
				foreach($xml->defines->criteriadefine->criteria as $tempcriteria) {
					$ccheck = $tempcriteria->id;
					if(!strcmp($ccheck, $temptransaction->criteria->criteriaid)) { 
						$criteriaid = $start_value;
						$get_criteriaid_counts = mysql_query("SELECT count(*) as count, max(criteriaid) as maxval from criteria");
						while($get_criteriaid_count = mysql_fetch_assoc($get_criteriaid_counts)) {
							if($get_criteriaid_count[count] > 0)
								$criteriaid = $get_criteriaid_count[maxval] + 1;
						}
						$cstring = $tempcriteria->latitude . ";" . $tempcriteria->longitude . ";" . $tempcriteria->radius . ";" . $tempcriteria->networktype . ";" . $tempcriteria->starttime . ";" . $tempcriteria->endtime . ";" . $tempcriteria->minimumbatterypower . ";" . $tempcriteria->minimumsignalstrength. ";" . $tempcriteria->networkcarrier. ";" . $tempcriteria->devicemodelname;
						$criteria_device_id = $tempcriteria->deviceid;
						if($tempcriteria->deviceid != '') { 
							$sql="INSERT INTO criteria (criteriaid, specification, deviceid) VALUES($criteriaid,'$cstring', '$tempcriteria->deviceid')";
							if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
						}
						if($tempcriteria->deviceid == '') {
							$sql="INSERT INTO criteria (criteriaid, specification) VALUES($criteriaid,'$cstring')";
							if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
						}
					}
				}
				$sql="INSERT INTO trans_criteria_link (criteriaid, transactionid) VALUES($criteriaid, $transactionid)";
				if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
 
				foreach($temptransaction->transfers[0]->transfer as $temptransferr) {
					$temptransferid = $temptransferr->transferid;
					$transfer_repeat = $temptransferr["repeat"];
					while($transfer_repeat > 0) {
						foreach($xml->defines->transferdefine->transfer as $temptransfer) {
							$tcheck = $temptransfer->id;
							if(!strcmp($tcheck, $temptransferid)) { 
								$transferid = $start_value;
								$get_transferid_counts = mysql_query("SELECT count(*) as count, max(transferid) as maxval from transfer");
								while($get_transferid_count = mysql_fetch_assoc($get_transferid_counts)) {
									if($get_transferid_count[count] > 0)
										$transferid = $get_transferid_count[maxval] + 1;
								}
								$datetime = idate("Y") . "-" . idate("m") . "-" . idate("d") . " " .  idate("H") . ":" . idate("i") . ":" . idate("s"); 
								if($temptransfer->bytes->explicit == 0) {
									$bytestostore = $temptransfer->bytes->noofbytes;
									$contenttostore = "";
									$protocoltype = "";
									$contenttype = "ASCII";
								}
								elseif($temptransfer->bytes->explicit == 1) {
									$tempcontentid = $temptransfer->bytes->contentid;
									foreach($xml->defines->contentdefine->content as $tempcontent) {
										$contentcheck = $tempcontent->contentid;	
										if(!strcmp($contentcheck, $tempcontentid)) { 
											$contenttostore = (string)$tempcontent->data;
											$bytestostore = mb_strlen($contenttostore, '8bit');
											$protocoltype = $tempcontent->protocol;
											$contenttype = $tempcontent->contenttype;
										}
									}
								}
								$tempexplicit = $temptransfer->bytes->explicit;
								$contenttostore = str_replace("'", "\'", $contenttostore);
								if($contenttype == "HEX")
									$bytestostore = ceil($bytestostore/2);
								else if($contenttype == "BINARY")
									$bytestostore = ceil($bytestostore/8);
								$bytestostore = $bytestostore + 26;
								$sql="INSERT INTO transfer (transferid, sourceip, destinationip, bytes, type, transferadded, packetdelay, explicit, content, noofpackets, protocoltype, portnumber, contenttype, response) VALUES($transferid,'$temptransfer->sourceip','$temptransfer->destinationip', $bytestostore, $temptransfer->type, '$datetime', $temptransfer->packetdelay, $tempexplicit, '$contenttostore', $temptransfer->noofpackets, '$protocoltype', $temptransfer->portnumber, '$contenttype', $temptransfer->response)";
								if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
				
								$sql="INSERT INTO trans_transfer_link (transferid, transactionid, orderno) VALUES($transferid,$transactionid, $order)";
								if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
								//$sql="INSERT INTO metricdata (metricid, transferid, transactionid, value) VALUES(9999, $transferid, $transactionid, 0)";
								//if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
								$order++;
							}
						}
						$transfer_repeat = $transfer_repeat - 1;
					}
				}
			}
			if($yesdone == 1)
			echo "$experiment_id";
		}
		else
		echo "Invalid account credentials.";
	}	
?>