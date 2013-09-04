
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
cri.specification specification,
cri.deviceid,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as location,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as networktype,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 3), ';', -1) as starttime,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 4), ';', -1) as endtime,
trf.sourceip as sourceip, trf.destinationip as destinationip, trf.bytes as bytes, trf.transferid as transferid,
trs.transactionid as transactionid, trf.type as type, trf.packetdelay, trf.explicit, substring(replace(replace(content,'\t',''), '\n\r', '\n'),1) content, trf.noofpackets, trf.portnumber, trf.contenttype, trf.response
from criteria cri, transfer trf, transaction1 trs, trans_criteria_link tcl, trans_transfer_link ttl
where cri.criteriaid = tcl.criteriaid
and trs.transactionid = tcl.transactionid
and trf.transferid = ttl.transferid
and ttl.transactionid = trs.transactionid
and trs.username = '$_GET[username]'
and trf.transferid in (SELECT transferid from metric m, metricdata md where m.metricid = md.metricid and m.metricid = 9999 and md.value = 0.00)
and '$_GET[time]' between SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 3), ';', -1) and SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 4), ';', -1)
and SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) = '$_GET[networktype]'
and SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) = '$_GET[city]'
order by ttl.orderno"); 
            $output="";
		$i = 0 ;
            while($pendingtestrow=mysql_fetch_assoc($pendingtestset))
     		{$output[]= $pendingtestrow;
		$output[$i][content] = str_replace("/", "/", $output[$i][content]);
		$i = $i + 1;
		}
            if($output)
                print(json_encode($output));
             else {
				$pendingtestset = mysql_query("SELECT 'NoPendingTransactions' as location, '' as starttime, '' as endtime, '' as clientip, '' as serverip, '' as bytes, '' as downbytes from transfer LIMIT 1");
				$pendingtestrow=mysql_fetch_assoc($pendingtestset);
				$output[]=$pendingtestrow;
				print(json_encode($output));    }    }				
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
