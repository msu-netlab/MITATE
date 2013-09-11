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
			$current_time = time();
			$yesdone = 0;
			if($_FILES["file"]["name"] != "") {
				if ($_FILES["file"]["error"] > 0) {
					echo "Error: " . $_FILES["file"]["error"] . "<br />";
				}
				else {
					$file_extension = end(explode(".", $_FILES["file"]["name"]));
					$file_name_without_extension = basename($_FILES["file"]["name"], ".xml");
					$final_file_path = $file_name_without_extension . $current_time . "." . $file_extension;
					move_uploaded_file($_FILES["file"]["tmp_name"],"user_accounts/$username/" . $final_file_path);		
					$yesdone = 1;
				}
			}
			else echo "Error: File not specified";
			$filepath = "user_accounts/" . $username . "/" . $final_file_path;
			$xml = simplexml_load_file("$filepath");
			$sql="INSERT INTO experiment (experiment_id, username, permission) VALUES($current_time, '$username', 'public')";
			if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
			foreach($xml->transactions->transaction as $temptransaction) {
				$order=1;
				$changeagain = rand(10, 10000);
				$change=rand(10, 10000);
				$transactionid = time() + ($change * 9655) - $changeagain;
				$transaction_count = $temptransaction["count"];
				if($transaction_count != "") { 
					$sql="INSERT INTO transaction1 (transactionid, username, count, original_count, experiment_id) VALUES($transactionid, '$username', $transaction_count, $transaction_count, $current_time)";
					if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
				}
				if($transaction_count == ""){
					$sql="INSERT INTO transaction1 (transactionid, username, experiment_id) VALUES($transactionid,'$username', $current_time)";
					if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
				}
				foreach($xml->defines->criteriadefine->criteria as $tempcriteria) {
					$ccheck = $tempcriteria->id;
					if(!strcmp($ccheck, $temptransaction->criteria->criteriaid)) { 
						$changeagain = rand(10, 10000);
						$change=rand(10, 10000);
						$criteriaid = time()+ ($change * 6546) - $changeagain;
						$cstring = $tempcriteria->latitude . ";" . $tempcriteria->longitude . ";" . $tempcriteria->radius . ";" . $tempcriteria->networktype . ";" . $tempcriteria->starttime . ";" . $tempcriteria->endtime;
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
								$changeagain = rand(10, 10000);
								$change=rand(10, 10000);
								$transferid= time()+ ($change * 9742) -$changeagain;
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
											$bytestostore = mb_strlen($contenttostore, '8bit') + 26;
											$protocoltype = $tempcontent->protocol;
											$contenttype = $tempcontent->contenttype;
										}
									}
								}
								$tempexplicit = $temptransfer->bytes->explicit;
								$contenttostore = str_replace("'", "\'", $contenttostore);
								//if($contenttype == "HEX")
								//$bytestostore = ceil($bytestostore/2) + 26;
								//else if($contenttype == "BINARY")
								//$bytestostore = ceil($bytestostore/8) + 26;
								//else
								//$bytestostore = $bytestostore + 26;
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
			echo "Your file with ID: $current_time has been uploaded successfully. ";
		}
		else
		echo "Invalid account credentials.";
	}	
?>