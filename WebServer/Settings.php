<?php include("usermenu.php"); 
if($_POST['action']=="Add Device")
printf("<script>location.href = 'addmobile.php'</script>");
if($_POST['action']=="Edit Device Settings")
printf("<script>location.href = 'editmobile.php'</script>");
if($_POST['action']=="Delete Device")
printf("<script>location.href = 'deletemobile.php'</script>");
?>
<br />
<center><font size="4">Below are your registered mobile devices!</font></center>
<br/>
<br/>
<table align="center" style="font-family:calibri; font-size:15;" border="1" width="80%"">
<th align="center">Device Name</th><th align="center">Poll Interval (min)</th><th align="center">Mobile Data Cap (MB/mo)</th><th align="center">AvailableMobile Data Cap (MB/mo)</th><th align="center">WiFi Data Cap (MB/mo)</th><th align="center">Available WiFi Data Cap (MB/mo)</th><th align="center">Battery Limit (%)</th>

<?php
$con = mysql_connect("localhost","root","root");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
$result = mysql_query("SELECT * FROM userdevice where username='$_COOKIE[username]'");
while($row = mysql_fetch_array($result))
  {

?>
<tr>
<input type="hidden" value="<?php echo $row['deviceid']; ?>" name="deviceid" />
<td align="center"><?php echo $row['devicename']; ?></td >
<td align="center"><?php echo $row['pollinterval']; ?></td>
<td align="center"><?php echo $row['datacap']; ?></td>
<td align="center"><?php echo $row['availabledata']; ?></td>
<td align="center"><?php echo $row['wificap']; ?></td>
<td align="center"><?php echo $row['availablewifi']; ?></td>
<td align="center"><?php echo $row['minbatterypower']; ?></td>
</tr>
<?php
}
?>
</table>
<br />
<br />
<form action="" method="POST">
<center>
<input type="submit" name="action" value="Add Device" />
<input type="submit" name="action" value="Edit Device Settings" />
<input type="submit" name="action" value="Delete Device" />
</center>
</form>
<?php include("bmenu.php");?>


