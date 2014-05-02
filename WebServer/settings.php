<?php session_start();
include('header.php');
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$con) {
	die('Website down for maintenance. We will be live soon.');
}
mysql_select_db($dbschemaname, $con);
if($_POST[cellular_credits] !='' && $_POST[wifi_credits] != '') {
	$sql="update usercredits set available_cellular_credits = $_POST[cellular_credits], available_wifi_credits = $_POST[wifi_credits] where username = '$_SESSION[mitateLoggedInUser]'";
	if (!mysql_query($sql, $con)) {
		die('Website down for maintenance. We will be live soon.');
	}
}
$tempcount=0;
while($tempcount < $_POST['rowCount']) {
	$tempcount = $tempcount + 1;
	$tempdeviceid = $_POST["deviceid" . $tempcount] ;
	$pollintervalvar = $_POST["pollinterval" . $tempdeviceid];
	$minbatterypowervar = $_POST["minbatterypower" . $tempdeviceid];
	if($minbatterypowervar != '' && $pollintervalvar != '') {
		$sql="update userdevice set pollinterval = $pollintervalvar, minbatterypower = $minbatterypowervar where deviceid = $tempdeviceid";
		if (!mysql_query($sql,$con)) {
			die('Website down for maintenance. We will be live soon.');
		}
	}
}
?>

<script type="text/javascript">
function validateAccountForm() {
    var formAccount = document.account;
	var formCredits = document.credits;
    if (formAccount.cellular_credits.value == "" || formAccount.wifi_credits.value == "") {
		alert("Please enter all the input fields.");
		return false;
    }
}
</script>


<?php
if($_SESSION[mitateLoggedInUser]) {
$getUserSettings = mysql_query("SELECT * from usercredits where username='$_SESSION[mitateLoggedInUser]';");
while($userSettingRow = mysql_fetch_array($getUserSettings))
  {
?>
<h3 style="text-decoration:underline">Account Settings:</h3>
<form action="" method="POST" name="account" onsubmit="return validateAccountForm();">
<table>
<tr>
<td style="font-size: 18;text-align: justify;">Available Cellular Credits (MB)</td>
<td style="margin:20px;">
<input style="font-size: 18;" type="text" name="cellular_credits" value="<?php echo $userSettingRow[available_cellular_credits];?>" />
</td>
</tr>
<tr>
<td style="font-size: 18;text-align: justify;">Available Wi-Fi Credits (MB)</td>
<td style="margin:20px;">
<input style="font-size: 18;" type="text" name="wifi_credits" value="<?php echo $userSettingRow[available_wifi_credits];?>" />
</td>
</tr>
<tr>
<td></td><td><input style="font-size: 18;" type="submit" value="Update" /></td>
</tr>
</table>
</form>
<br />
<h3 style="text-decoration:underline">Device Settings:</h3>

<?php
}
$deviceCount = 0;
$getUserDeviceCount = mysql_query("SELECT count(*) as count FROM userdevice where username='$_SESSION[mitateLoggedInUser]';");
while($deviceCounts = mysql_fetch_array($getUserDeviceCount)) {
	$deviceCount = $deviceCounts[count];
}
if($deviceCount > 0) {
	?>
		<form action="" method="POST" name="credits">
	<table>
	<th style="font-size: 18;">Device Name</th><th style="font-size: 18;">Poll Interval (mins)</th><th style="font-size: 18;">Min. Battery (%)</th>
	<?php
	$getUserDeviceSettings = mysql_query("SELECT username, devicename, deviceid, pollinterval, minbatterypower FROM userdevice where username='$_SESSION[mitateLoggedInUser]';");
	$rowCount=0;
	while($deviceSettingRow = mysql_fetch_array($getUserDeviceSettings)) {
		$rowCount = $rowCount + 1;	
?>	
		<tr>
		<input type="hidden" value="<?php echo $deviceSettingRow['deviceid']; ?>" name="deviceid<?php echo $rowCount; ?>" />
		<td>
		<label style="font-size: 18;"><?php echo $deviceSettingRow['devicename']; ?></label>
		</td>
		<td>
		<input style="font-size: 18;" type="text" value="<?php echo $deviceSettingRow['pollinterval']; ?>" name="pollinterval<?php echo $deviceSettingRow['deviceid']; ?>" />
		</td>
		<td>
		<input style="font-size: 18;" type="text" value="<?php echo $deviceSettingRow['minbatterypower']; ?>" name="minbatterypower<?php echo $deviceSettingRow['deviceid']; ?>" />
		</td>
		</tr>
<?php
	}
	?>
	<tr><td></td><td><input style="font-size: 18;" type="submit" value="Update" /></td></tr>
	<tr>
	<input type="hidden" value="<?php echo $rowCount; ?>" name="rowCount" />
	</tr>
	</table>
		</form>
<?php
}
else
	echo '<label style="font-size: 18;">You do not have any registered devices.</label>';
}
else
	printf('<script>document.location.href="mitate_signin.php";</script>')
?>