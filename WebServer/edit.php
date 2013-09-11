<?php include("usermenu.php"); 
$con = mysql_connect("localhost","mitate","Database4Mitate");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
?>

<?php
if(!empty($_POST) && ($_POST['idevicename'] == "" || $_POST['ipollinterval'] == "" || $_POST['idatacap'] == "" || $_POST['iwificap'] == ""))
    {
  echo "Please enter new values";
      }
	elseif($_POST['idevicename'] !="")
    {
mysql_query("update usermobile set devicename='$_POST[idevicename]',  pollinterval=$_POST[ipollinterval], datacap=$_POST[idatacap], wificap=$_POST[iwificap] where imei='$_POST[iimei]'");
echo "Changes saved";
}
?>

<?php
if($_GET['imei'] == "")
printf("<script>location.href = 'index.php'</script>");
else
{
$result = mysql_query("SELECT * FROM usermobile");
$k=0;
while($row = mysql_fetch_array($result))
  {
if($row['imei']==$_GET['imei'])
{
$k=1;
break;
}
}
if($k==1)
{
?>
<center><p style="font-family:calibri;"><font size=4">Edit Settings for IMEI <?php echo $_GET['imei']; ?></font></p>
<br />
<form action="" method="POST">
<table style="font-family:calibri;">
<tr><td></td>
<td align="center"> Device Name</td><td align="center">Poll Interval</td><td align="center">Data Cap (MB/mo)</td><td align="center">Wi-Fi Cap (MB/mo)</td>
</tr>
<tr>
<td><input type="hidden" value="<?php echo $row[imei]; ?>" name="iimei" /></td>
<td align="center"><input type="text" value="<?php echo $row[devicename]; ?>" name="idevicename" /></td>
<td align="center"><input type="text" value="<?php echo $row[pollinterval]; ?>" name="ipollinterval" /></td>
<td align="center"><input type="text" value="<?php echo $row[datacap]; ?>" name="idatacap"/></td>
<td align="center"><input type="text" value="<?php echo $row[wificap]; ?>" name="iwificap"/></td>
</tr>
<tr>
<td></td><td align="left"><input type="submit" value="Save Changes" /></td><td></td><td></td><td></td>
</tr>
</table>
</form>
<?php
}
else
{
echo "<center>No record found for IMEI: $_GET[imei]</center>";
}
}
?>


<?php include("bmenu.php");?>


