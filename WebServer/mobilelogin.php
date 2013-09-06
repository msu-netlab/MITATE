<?php
if($_GET['username']!="" || $_GET['password']!="")
{
	$dbhostname = "localhost";
    $dbusername = "root";
    $dbpassword = "root";
    $dbschemaname = "mitate";
    $dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
    if (!$dbconnection)	{
        die('Could not connect: ' . mysql_error());
    }
    mysql_select_db($dbschemaname, $dbconnection);
    $loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$_GET[username]' and password = '$_GET[password]'");
    if ($loginresultset) {
        $loginresultrow = mysql_fetch_assoc($loginresultset);
        if ($loginresultrow['status'] == "1") {
            $pendingtestset = mysql_query("SELECT
			trf.sourceip as sourceip, trf.destinationip as destinationip, trf.bytes as bytes, trf.transferid as transferid,
			trs.transactionid as transactionid, trf.type as type, trf.packetdelay, trf.explicit, substring(replace(replace(content,'\t',''), '\n\r', '\n'),1) content, trf.noofpackets, trf.portnumber, trf.contenttype, trf.response
			from criteria cri, transfer trf, transaction1 trs, trans_criteria_link tcl, trans_transfer_link ttl
			where cri.criteriaid = tcl.criteriaid
			and trs.transactionid = tcl.transactionid
			and trs.count > 0
			and (find_in_set('$_GET[deviceid]', cast(REPLACE(cri.deviceid, ' ', '') as char)) > 0 or cri.deviceid = 'client')
			and trf.transferid = ttl.transferid
			and ttl.transactionid = trs.transactionid
			and trs.transactionid not in (select transactionid from transaction_fetched where deviceid = '$_GET[deviceid]')
			and '$_GET[time]' between SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 5), ';', -1) and SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 6), ';', -1)
			and SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 4), ';', -1) = '$_GET[networktype]'	
			and ((6378.137 * ACos((Cos(cast($_GET[latitude] as decimal)*(22/(180*7)))) * (Cos(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))) * (Cos((cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as decimal) - cast($_GET[longitude] as decimal))*(22/(180*7)))) + Sin(cast($_GET[latitude] as decimal)*(22/(180*7))) * (Sin(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))))) <= (cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 3), ';', -1) as decimal)*(22/(180*7))) 
			or (6378.137 * ACos((Cos(cast($_GET[latitude] as decimal)*(22/(180*7)))) * (Cos(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))) * (Cos((cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as decimal) - cast($_GET[longitude] as decimal))*(22/(180*7)))) + Sin(cast($_GET[latitude] as decimal)*(22/(180*7))) * (Sin(cast(SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as decimal)*(22/(180*7)))))) = 0 )
			order by ttl.orderno"); 
            $output="";
			$i = 0 ;
			$transaction_id_array = "";
            while($pendingtestrow=mysql_fetch_assoc($pendingtestset))
     		{
				$output[]= $pendingtestrow;
				$output[$i][content] = str_replace("/", "/", $output[$i][content]);
				$output_transaction_id = $output[$i][transactionid];
				$transaction_id_array[$i] = $output_transaction_id;
				$i = $i + 1;
			}
            if($output) {
				$temp_count = count(array_unique($transaction_id_array));
				print(json_encode($output));
				while($temp_count > 0) {
					$transaction_count_reduce = array_unique($transaction_id_array)[$temp_count - 1];
					$sql_store_deviceid ="INSERT INTO transaction_fetched (transactionid, deviceid) VALUES($transaction_count_reduce, '$_GET[deviceid]')";
					if (!mysql_query($sql_store_deviceid, $dbconnection)) {die('Error: ' . mysql_error());}
					mysql_query("update transaction1 set count = count - 1 where transactionid = $transaction_count_reduce", $dbconnection);
					$temp_count = $temp_count - 1;
				}							
			}
             else {
				$pendingtestset = mysql_query("SELECT 'NoPendingTransactions' as location, '' as starttime, '' as endtime, '' as clientip, '' as serverip, '' as bytes, '' as downbytes from transfer LIMIT 1");
				$pendingtestrow=mysql_fetch_assoc($pendingtestset);
				$output[]=$pendingtestrow;
				print(json_encode($output));
			}
		}				
        else if ($loginresultrow['status'] == "0") {
            $pendingtestset = mysql_query("SELECT 'InvalidLogin' as location, '' as starttime, '' as endtime, '' as clientip, '' as serverip, '' as bytes, '' as downbytes from transfer LIMIT 1");
            $pendingtestrow=mysql_fetch_assoc($pendingtestset);
            $output[]=$pendingtestrow;
            print(json_encode($output));
        }				
    }
    mysql_close();
}
?>
