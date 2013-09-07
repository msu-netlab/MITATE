<?php include("usermenu.php"); 
$message="";
$k=1;
if(empty($_POST) && ($_POST['devicetype'] == "" || $_POST['devicename'] == "" || $_POST['pollinterval'] == "" || $_POST['datacap'] == "" || $_POST['wifi'] == "" || $_POST['minbatterypower'] == ""))
{
$k=0;
}
elseif($k==1)
{
$con = mysql_connect("localhost","root","root");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
  $change=rand(10, 10000);
  $deviceid = substr(time(), 0, 10);
  
  $sql="INSERT INTO userdevice (username, devicename, pollinterval, datacap, availabledata, wificap, availablewifi, deviceid, minbatterypower) VALUES ('$_COOKIE[username]', '$_POST[devicename]', $_POST[pollinterval], $_POST[datacap], $_POST[datacap], $_POST[wificap], $_POST[wificap], $deviceid, $_POST[minbatterypower])";
  
  if (!mysql_query($sql,$con))
  {
  die('Error: ' . mysql_error());
  }
else
  $message="Your mobile device has been added successfully";
}
?>
<head>
<script language="javascript">
       
        function validateForm() {
            var form = document.MobileRegForm;
           

            if (form.devicename.value == "") {
                alert("Please Enter the Device Name");
                form.devicename.focus();
                return false;
            }
			if (form.pollinterval.value == "") {
                alert("Please Enter the Polling Interval");
                form.pollinterval.focus();
                return false;
            }
			if (form.datacap.value == "") {
                alert("Please Enter the Data Cap");
                form.datacap.focus();
                return false;
            }
			if (form.wificap.value == "") {
                alert("Please Enter the Wi-Fi Cap");
                form.wificap.focus();
                return false;
            }
			if (form.minbatterypower.value == "") {
                alert("Please Enter the Minimum Battery Power Req.");
                form.minbatterypower.focus();
                return false;
            }
		}
</script>
</head
<br />
<center><font size="4">Add a Mobile Device</font></center>
<br />
<br />
<form action="" method="POST" name="MobileRegForm">
<table align="center" style="font-family:calibri; font-size:15;"  width="70%"">

<th align="center">Device Name</th><th align="center">Poll Interval (min)</th><th align="center">Mobile Data Cap (MB/mo)</th><th align="center">WiFi Data Cap (MB/mo)</th><th align="center">Battery Limit (%)</th>
<tr>
<td>
<input type="text" value="" name="devicename" />
</td>
<td>
<input type="text" value="" name="pollinterval" />
</td>
<td>
<input type="text" value="" name="datacap" />
</td>
<td>
<input type="text" value="" name="wificap" />
</td>
<td>
<input type="text" value="" name="minbatterypower" />
</td>
</tr>
<tr>
<td><input type="submit" name="action" value="Save" onclick="return validateForm()" /></td>
</tr>
</table>
</form>
<br />
<br />
<center><label><?php echo $message; ?></label></center>

<?php include("bmenu.php");?>


