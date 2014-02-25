<head>
<title>MITATE</title>
</head>
<body style="font-family: Calibri;">
<?php include("usermenu.php"); ?>
<center>
<h2>Your Completed tests are below</h2>
<br />

<?php
    $dbhostname = "localhost";
    $dbusername = "mitate";
    $dbpassword = "Database4Mitate";
    $dbschemaname = "mitate";
    
    $dbconnection = mysql_connect($dbhostname, $dbusername, $dbpassword);
    if (!$dbconnection)	{
        die('Could not connect: ' . mysql_error());
    }
    mysql_select_db($dbschemaname, $dbconnection);
	$completedtestset = mysql_query("SELECT
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 1), ';', -1) as location,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 2), ';', -1) as networktype,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 3), ';', -1) as starttime,
SUBSTRING_INDEX(SUBSTRING_INDEX(cri.specification, ';', 4), ';', -1) as endtime,
trf.sourceip as sourceip, trf.destinationip as destinationip, trf.bytes as bytes, trf.transferid as transferid, trf.packetdelay as packetdelay,
trs.transactionid as transactionid, trf.type as type, trf.noofpackets as noofpackets, trf.protocoltype, trf.portnumber as port
from criteria cri, transfer trf, transactions trs, trans_criteria_link tcl, trans_transfer_link ttl
where cri.criteriaid = tcl.criteriaid
and trs.transactionid = tcl.transactionid
and trf.transferid = ttl.transferid
and ttl.transactionid = trs.transactionid
and trs.username = '$_COOKIE[username]'
and trf.transferid in (SELECT transferid from metric m, metricdata md where m.metricid = md.metricid and m.metricid = 9999 and md.value = 1.00)
order by ttl.transactionid, ttl.transferid");
?>

<table border=1>
  <th>S.No.</th><th>Location</th><th>Network Type</th><th>Start Time</th><th>End Time</th>
  <th>Source IP</th><th>Destination IP</th><th>Bytes</th><th>Type</th><th>Packet Delay</th><th>No. of Packets</th><th>Protocol</th><th>Port</th>

  <?php
$rowcount=0;
while($row = mysql_fetch_array($completedtestset))
  {
$rowcount= $rowcount+1;

  ?>
  
  <tr><td align="center"><?php echo $rowcount; ?></td><td align="center"><?php echo $row['location']; ?></td><td align="center"><?php echo $row['networktype']; ?></td><td align="center"><?php echo $row['starttime']; ?></td><td align="center"><?php echo $row['endtime']; ?></td>
  <td align="center"><?php echo $row['sourceip']; ?></td><td align="center"><?php echo $row['destinationip']; ?></td><td align="center"><?php echo $row['bytes']; ?></td><td align="center"><?php echo $row['type']; ?></td><td align="center"><?php echo $row['packetdelay']; ?></td><td align="center"><?php echo $row['noofpackets']; ?></td><td align="center"><?php echo $row['protocoltype']; ?></td><td align="center"><?php echo $row['port']; ?></td>
  </tr>
  
  <?php
  
  }
//echo $completedtestset;
mysql_close();
	?>
</table>
<br />
<br />
</center>
<?php include("bmenu.php"); ?>
</body>
