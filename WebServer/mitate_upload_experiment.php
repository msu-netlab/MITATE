<?php
libxml_use_internal_errors(true); 

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
					mkdir("user_accounts/$username/experiments/$experiment_id", 0777);
					move_uploaded_file($_FILES["file"]["tmp_name"],"user_accounts/$username/experiments/$experiment_id/" . $final_file_path);					
					$filename_validate = "user_accounts/" . $username . "/experiments/$experiment_id/" . $final_file_path;
					$xsd_document = 'sample/Mitate_Sample_Configuration_File_XML_Format.xsd';
					$dom = new DomDocument(); 
					if (!$dom->load($filename_validate)) { 
						libxml_display_errors();
						Delete("user_accounts/" . $username . "/experiments/" . $experiment_id);
					}
					else {
						if (!$dom->schemaValidate($xsd_document)) {			
							libxml_display_errors();  
							Delete("user_accounts/" . $username . "/experiments/" . $experiment_id);
						}
						else {
							$urltopost = "http://mitate.cs.montana.edu/mitate_count_credit.php";
							$datatopost = array ("XMLFilePath" => "user_accounts/$username/experiments/$experiment_id/$final_file_path");
							$ch = curl_init ($urltopost);
							curl_setopt ($ch, CURLOPT_POST, true);
							curl_setopt ($ch, CURLOPT_POSTFIELDS, $datatopost);
							curl_setopt ($ch, CURLOPT_RETURNTRANSFER, true);
							$returndata = curl_exec ($ch);
							$total_credits_in_xml = explode(":", $returndata);
							$get_user_available_credits = mysql_query("SELECT sum(available_cellular_credits) as availabledata, sum(available_wifi_credits) as availablewifi FROM usercredits where username = '$username'");
							$user_data_credits = mysql_fetch_assoc($get_user_available_credits);
							if($user_data_credits[availabledata] >= $total_credits_in_xml[0]/1024.0 && $user_data_credits[availablewifi] >= $total_credits_in_xml[1]/1024.0) {
								$filepath = "user_accounts/" . $username . "/experiments/$experiment_id/" . $final_file_path;
								$xml = simplexml_load_file("$filepath");
								$sql="INSERT INTO experiment (experiment_id, username, permission, cellulardata, wifidata) VALUES($experiment_id, '$username', 'private', $total_credits_in_xml[0]/1024.0, $total_credits_in_xml[1]/1024.0)";
								if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
								foreach($xml->transactions->transaction as $temptransaction) {
									$order=1;
									$transactionid = $start_value;
									$get_transactionid_counts = mysql_query("SELECT count(*) as count, max(transactionid) as maxval from transactions");
									while($get_transactionid_count = mysql_fetch_assoc($get_transactionid_counts)) {
										if($get_transactionid_count[count] > 0)
											$transactionid = $get_transactionid_count[maxval] + 1;
									}
									$transaction_count = $temptransaction["count"];
									if($transaction_count != "") { 
										$sql="INSERT INTO transactions (transactionid, username, count, original_count, experiment_id) VALUES($transactionid, '$username', $transaction_count, $transaction_count, $experiment_id)";
										if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
									}
									if($transaction_count == ""){
										$sql="INSERT INTO transactions (transactionid, username, experiment_id) VALUES($transactionid,'$username', $experiment_id)";
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
											$criteria_devicemodelname = $tempcriteria->devicemodelname;
											if($criteria_devicemodelname == '')
												$criteria_devicemodelname = 'allDeviceModelNames';
											$criteria_networkcarrier = $tempcriteria->networkcarrier;
											if($criteria_networkcarrier == '')
												$criteria_networkcarrier = 'allNetworkCarriers';
											$criteria_minimumsignalstrength = $tempcriteria->minimumsignalstrength;
											if($criteria_minimumsignalstrength == '')
												$criteria_minimumsignalstrength = '-1000';
											$criteria_minimumbatterypower = $tempcriteria->minimumbatterypower;
											if($criteria_minimumbatterypower == '')
												$criteria_minimumbatterypower = '0';
											$criteria_deviceid = $tempcriteria->deviceid;
											if($criteria_deviceid == '')
												$criteria_deviceid = 'client';
											$criteria_networktype = $tempcriteria->networktype;
											if($criteria_networktype == '')
												$criteria_networktype = 'allNetworkTypes';
											$criteria_starttime = $tempcriteria->starttime;
											if($criteria_starttime == '')
												$criteria_starttime = '000001';
											$criteria_endtime = $tempcriteria->endtime;
											if($criteria_endtime == '')
												$criteria_endtime = '235959';
											$cstring = $tempcriteria->latitude . ";" . $tempcriteria->longitude . ";" . $tempcriteria->radius . ";" . $criteria_networktype . ";" . $criteria_starttime . ";" . $criteria_endtime . ";" . $criteria_minimumbatterypower . ";" . $criteria_minimumsignalstrength . ";" . $criteria_networkcarrier . ";" . $criteria_devicemodelname;	 
											$sql="INSERT INTO criteria (criteriaid, specification, deviceid) VALUES($criteriaid,'$cstring', '$criteria_deviceid')";
											if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
										}
									}
									$sql="INSERT INTO trans_criteria_link (criteriaid, transactionid) VALUES($criteriaid, $transactionid)";
									if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());} 
									foreach($temptransaction->transfers[0]->transfer as $temptransferr) {
										$temptransferid = $temptransferr->transferid;
										$transfer_repeat = $temptransferr["repeat"];
										$transfer_delay = $temptransferr["delay"];
										if($trasnsfer_delay == "") 
											$trasnsfer_delay = 0;
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
																$bytestostore = mb_strlen($contenttostore, '8bit') - (3 * substr_count($contenttostore, '\r\n')) ;
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
													$bytestostore = $bytestostore + 26 + substr_count($contenttostore, '\r\n');
													$sql="INSERT INTO transfer (transferid, sourceip, destinationip, bytes, type, transferadded, packetdelay, explicit, content, noofpackets, protocoltype, portnumber, contenttype, response, delay) VALUES($transferid,'$temptransfer->sourceip','$temptransfer->destinationip', $bytestostore, $temptransfer->type, '$datetime', $temptransfer->packetdelay, $tempexplicit, '$contenttostore', $temptransfer->noofpackets, '$protocoltype', $temptransfer->portnumber, '$contenttype', $temptransfer->response, $transfer_delay)";
													if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}	
													$sql="INSERT INTO trans_transfer_link (transferid, transactionid, orderno) VALUES($transferid,$transactionid, $order)";
													if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
													$order++;
												}
											}
											$transfer_repeat = $transfer_repeat - 1;
										}
									}
								}		
								$yesdone = 1;
								echo "$experiment_id";
								$sql="update usercredits set available_cellular_credits = (available_cellular_credits - ($total_credits_in_xml[0]/1024.0)), available_wifi_credits = (available_wifi_credits - ($total_credits_in_xml[1]/1024.0)) where username = '$username'";
								if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
							}
							else {
								if($user_data_credits[availabledata] < $total_credits_in_xml[0]/1024.0)
									echo "You do not have enough cellular data credits.";
								elseif ($user_wifi_credits[availablewifi] < $total_credits_in_xml[1]/1024.0)
									echo "You do not have enough Wi-Fi data credits.";
							}
						}
					}	
				}
			}
			else 
				echo "Error: File not specified";
		}
		else
			echo "Invalid account credentials.";
		}	

function libxml_display_error($error) { 
	$return = "\n"; 
	switch ($error->level) { 
		case LIBXML_ERR_WARNING: $return .= "Warning $error->code: "; 
								break; 
		case LIBXML_ERR_ERROR: $return .= "Error $error->code: "; 
								break; 
		case LIBXML_ERR_FATAL: $return .= "Fatal Error $error->code: "; 
								break; 
	} 
	$return .= trim($error->message); 
	if ($error->file) { 
		$return .= " in $error->file"; 
	} 
	$return .= " on line $error->line\n"; 
	return $return; 
} 

function libxml_display_errors() { 
	$errors = libxml_get_errors(); 
	foreach ($errors as $error) { 
		print libxml_display_error($error); 
	} 
	libxml_clear_errors(); 
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