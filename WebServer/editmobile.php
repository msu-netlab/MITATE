<?php include("usermenu.php"); 
$con = mysql_connect("localhost","root","root");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
$message="";
$k=1;
if(empty($_POST))
{
$k=0;
}
elseif($k==1)
{
$tempcount=0;
while($tempcount < $_POST['count'])
{
$tempcount = $tempcount + 1;
$tempdeviceid = $_POST["deviceid" . $tempcount] ;
$devicenamevar = "devicename" . $tempdeviceid;
$pollintervalvar = "pollinterval" . $tempdeviceid;
$datacapvar = "datacap" . $tempdeviceid;
$wificapvar = "wificap" . $tempdeviceid;
$minbatterypowervar = "minbatterypower" . $tempdeviceid;

$result = mysql_query("SELECT * FROM userdevice where username='$_COOKIE[username]' and deviceid = $tempdeviceid");
while($row = mysql_fetch_array($result))
  {
  $oldavailabledata= $row['availabledata'];
  $oldavailablewifi= $row['availablewifi'];
  $olddatacap = $row['datacap'];
  $oldwificap = $row['wificap'];
  break;
  }
  $newavailabledata = $oldavailabledata + $_POST[$datacapvar] - $olddatacap ;
  $newavailablewifi = $oldavailablewifi + $_POST[$wificapvar] - $oldwificap ;
  
$sql="update userdevice set devicename = '$_POST[$devicenamevar]', pollinterval = $_POST[$pollintervalvar], datacap = $_POST[$datacapvar], availabledata = $newavailabledata, wificap = $_POST[$wificapvar], availablewifi = $newavailablewifi, minbatterypower = $_POST[$minbatterypowervar] where deviceid = $tempdeviceid";
  
  if (!mysql_query($sql,$con))
  {
  die('Error: ' . mysql_error());
  }
else
  $message="Your mobile device settings have been updated successfully";
}
}
?>
<br />
<center><font size="4">Edit your registered mobile devices!</font></center>
<br/>
<br/>
<form action="" method="post">
<table align="center" style="font-family:calibri; font-size:15;" width="70%"">
<th align="center">Device Name</th><th align="center">Poll Interval (min)</th><th align="center">Mobile Data Cap (MB/mo)</th><th align="center">WiFi Data Cap (MB/mo)</th><th align="center">Battery Limit (%)</th>

<?php
$result = mysql_query("SELECT * FROM userdevice where username='$_COOKIE[username]'");
$count=0;
while($row = mysql_fetch_array($result))
  {
$count = $count + 1;
?>
<tr>
<input type="hidden" value="<?php echo $row['deviceid']; ?>" name="deviceid<?php echo $count; ?>" />
<td align="center"><input type="text" value="<?php echo $row['devicename']; ?>" name="devicename<?php echo $row['deviceid']; ?>" /></td >
<td align="center"><input type="text" value="<?php echo $row['pollinterval']; ?>" name="pollinterval<?php echo $row['deviceid']; ?>" /></td>
<td align="center"><input type="text" value="<?php echo $row['datacap']; ?>" name="datacap<?php echo $row['deviceid']; ?>" /></td>
<td align="center"><input type="text" value="<?php echo $row['wificap']; ?>" name="wificap<?php echo $row['deviceid']; ?>" /></td>
<td align="center"><input type="text" value="<?php echo $row['minbatterypower']; ?>" name="minbatterypower<?php echo $row['deviceid']; ?>" /></td>
</tr>
<?php
}
?>
<tr>
<td><input type="submit" value="Update" /></td>
<input type="hidden" value="<?php echo $count; ?>" name="count" />
</tr>
</table>
</form>
<br />
<br />
<center><label><?php echo $message; ?></label></center>
<?php include("bmenu.php");?>


