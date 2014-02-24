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
		if($_FILES["file"]["name"] != "") {
			if ($_FILES["file"]["error"] > 0) {
				echo "Error: " . $_FILES["file"]["error"] . "<br />";
			}
			else {
				$file_extension = end(explode(".", $_FILES["file"]["name"]));
				$file_name_without_extension = basename($_FILES["file"]["name"], ".xml");
				$final_file_path = $file_name_without_extension . $experiment_id . "." . $file_extension;
				mkdir("user_accounts/$username/countcredit/$experiment_id", 0777);
				move_uploaded_file($_FILES["file"]["tmp_name"],"user_accounts/$username/countcredit/$experiment_id/" . $final_file_path);					
				$filename_validate = "user_accounts/" . $username . "/countcredit/$experiment_id/" . $final_file_path;
				$xsd_document = 'sample/Mitate_Sample_Configuration_File_XML_Format.xsd';
				$dom = new DomDocument(); 
				if (!$dom->load($filename_validate)) { 
					libxml_display_errors();
					Delete("user_accounts/" . $username . "/countcredit/" . $experiment_id);
				}
				else {
					if (!$dom->schemaValidate($xsd_document)) {			
						libxml_display_errors();  
						Delete("user_accounts/" . $username . "/countcredit/" . $experiment_id);
					}
					else {
						$count_cellular_credits = 0;
						$count_wifi_credits = 0;
						$criteria_networktype = "";
						$filepath = $filename_validate;
						$xml = simplexml_load_file("$filepath");
						echo $xml;
						foreach($xml->transactions->transaction as $temptransaction) {
							$transaction_count = $temptransaction["count"];
							foreach($xml->defines->criteriadefine->criteria as $tempcriteria) {
								$ccheck = $tempcriteria->id;
								if(!strcmp($ccheck, $temptransaction->criteria->criteriaid)) { 
									$criteria_networktype = $tempcriteria->networktype;
								}
							}
							foreach($temptransaction->transfers[0]->transfer as $temptransferr) {
								$temptransferid = $temptransferr->transferid;
								$transfer_repeat = $temptransferr["repeat"];
								foreach($xml->defines->transferdefine->transfer as $temptransfer) {
									$tcheck = $temptransfer->id;
									if(!strcmp($tcheck, $temptransferid)) { 
										if($temptransfer->bytes->explicit == 0) {
											$bytestostore = $temptransfer->bytes->noofbytes;
											$contenttype = "";
										}
										elseif($temptransfer->bytes->explicit == 1) {
											$tempcontentid = $temptransfer->bytes->contentid;
											foreach($xml->defines->contentdefine->content as $tempcontent) {
												$contentcheck = $tempcontent->contentid;	
												if(!strcmp($contentcheck, $tempcontentid)) { 
													$contenttostore = (string)$tempcontent->data;
													$contenttype = $tempcontent->contenttype;
													$bytestostore = mb_strlen($contenttostore, '8bit') - (3 * substr_count($contenttostore, '\r\n')) ;
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
										if ($criteria_networktype == "wifi")
											$count_wifi_credits = $count_wifi_credits + ($transfer_repeat * $temptransfer->noofpackets * $bytestostore);
										elseif ($criteria_networktype == "cellular")
											$count_cellular_credits = $count_cellular_credits + ($transfer_repeat * $temptransfer->noofpackets * $bytestostore);
									}
								}
							}
							$count_wifi_credits = $count_wifi_credits * $transaction_count;
							$count_cellular_credits = $count_cellular_credits * $transaction_count;
						}	
						echo "Cellular Data: $count_cellular_credits Bytes, Wi-Fi Data: $count_wifi_credits Bytes";
						Delete("user_accounts/" . $username . "/countcredit/" . $experiment_id);
					}
				}
			}
		}
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