<?php
$count_cellular_credits = 0;
$count_wifi_credits = 0;
$criteria_networktype = "";
$filepath = $_POST[XMLFilePath];
$xml = simplexml_load_file("$filepath");
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
				if($contenttype == "")
					$bytestostore = $bytestostore - 26;
				if ($criteria_networktype == "wifi")
					$count_wifi_credits = $count_wifi_credits + ($transfer_repeat * $bytestostore);
				elseif ($criteria_networktype == "cellular")
					$count_cellular_credits = $count_cellular_credits + ($transfer_repeat * $bytestostore);
			}
		}
	}
	$count_wifi_credits = $count_wifi_credits * $transaction_count;
	$count_cellular_credits = $count_cellular_credits * $transaction_count;
}	
echo "$count_cellular_credits:$count_wifi_credits";
?>