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
if($_POST["check" . $tempdeviceid] == 1)
{
$sql = "delete from userdevice where deviceid = $tempdeviceid";
if (!mysql_query($sql,$con))
  {
  die('Error: ' . mysql_error());
  }
else
  $message="Your mobile device has been deleted successfully";
}
}
}
?>
<br />
<center><font size="4">Delete your registered mobile devices!</font></center>
<br/>
<br/>
<form action="" method="POST">
<table align="center" style="font-family:calibri; font-size:15;" width="20%"">
<?php
$result = mysql_query("SELECT * FROM userdevice where username='$_COOKIE[username]'");
$count=0;
while($row = mysql_fetch_array($result))
  {
$count = $count + 1;
?>
<tr>
<input type="hidden" value="<?php echo $row['deviceid']; ?>" name="deviceid<?php echo $count; ?>" />
<td><input type="checkbox" value="1" name="check<?php echo $row['deviceid']; ?>" /><?php echo $row['devicename']; ?></td>
</tr>
<?php
}
?>
<tr>
<td><input type="submit" value="Delete" /></td>
<input type="hidden" value="<?php echo $count; ?>" name="count" />
</tr>
</table>
</form>
<br />
<br />
<center><label><?php echo $message; ?></label></center>
<?php include("bmenu.php");?>
