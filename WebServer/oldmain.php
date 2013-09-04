
<html>
<head>
<title>MITATE</title>
</head>
<body style="font-family: Calibri;">
<?php include("usermenu.php"); ?>
<center>
<h2>Upload a file with your test parameters or <a target="_blank" href="http://54.243.186.107/sample/samplemnep.xml" >Click Here</a> for a sample</h2>
<br />
<form action="" method="post"
enctype="multipart/form-data">

<table style="font-family: Calibri;">
<tr><td>File:</td><td><input type="file" name="file" id="file" /> </td><td><input type="submit" value="Submit" name="submit" /></td></tr>
</table>

</form>


<?php
$yesdone = 0;
if($_FILES["file"]["name"] != "")
{
if ($_FILES["file"]["error"] > 0)
  {
  echo "Error: " . $_FILES["file"]["error"] . "<br />";
  }
else
{
if (file_exists("$_COOKIE[username]/" . $_FILES["file"]["name"]))
      {
      echo $_FILES["file"]["name"] . " already exists in your records. ";
      }
    else
      {
      move_uploaded_file($_FILES["file"]["tmp_name"],
      "$_COOKIE[username]/" . $_FILES["file"]["name"]);
$yesdone = 1;
      }

  }
}
?>

<?php
$con = mysql_connect("localhost","root","root");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mnepp", $con);

$username = $_COOKIE[username];
$filepath = $username . "/" . $_FILES["file"]["name"];
$xml = simplexml_load_file("$filepath");


foreach($xml->transactions->transaction as $temptransaction)
  {
  $order=1;
  $changeagain = rand(10, 10000);
  $change=rand(10, 10000);
  $transactionid = time() + ($change * 9655) - $changeagain;
  $sql="INSERT INTO transaction1 (transactionid, username) VALUES($transactionid,'$username')";
	if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
  foreach($xml->defines->criteriadefine->criteria as $tempcriteria)
  {
  $ccheck = $tempcriteria->id;
  if(!strcmp($ccheck, $temptransaction->criteria->criteriaid))
  { 
$changeagain = rand(10, 10000);
  $change=rand(10, 10000);
  $criteriaid = time()+ ($change * 6546) - $changeagain;
  $cstring = $tempcriteria->location . ";" . $tempcriteria->networktype . ";" . $tempcriteria->starttime . ";" . $tempcriteria->endtime;
   $sql="INSERT INTO criteria (criteriaid, specification) VALUES($criteriaid,'$cstring')";
	if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  }
  }
  
  $sql="INSERT INTO trans_criteria_link (criteriaid, transactionid) VALUES($criteriaid, $transactionid)";
	if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
  foreach($xml->transactions->transaction->transfers->transfer as $temptransferr)
  {
$temptransferid = $temptransferr->transferid;
foreach($xml->defines->transferdefine->transfer as $temptransfer)
  {
  $tcheck = $temptransfer->id;
  if(!strcmp($tcheck, $temptransferid))
  { 
$changeagain = rand(10, 10000);
  $change=rand(10, 10000);
  $transferid= time()+ ($change * 9742) -$changeagain;
  $datetime = idate("Y") . "-" . idate("m") . "-" . idate("d") . " " .  idate("H") . ":" . idate("i") . ":" . idate("s");
  $sql="INSERT INTO transfer (transferid, sourceip, destinationip, bytes, type, transferadded, packetdelay) VALUES($transferid,'$temptransfer->sourceip','$temptransfer->destinationip', $temptransfer->bytes, $temptransfer->type, '$datetime', $temptransfer->packetdelay)";
  if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
  $sql="INSERT INTO trans_transfer_link (transferid, transactionid, orderno) VALUES($transferid,$transactionid, $order)";
  if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
  $sql="INSERT INTO metricdata (metricid, transferid, transactionid, value) VALUES(9999, $transferid, $transactionid, 0)";
  if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
  
  $order++;
  }
  }
  }
  }
if($yesdone == 1)
    echo "Your file has been stored successfully";
?>
<br />
<br />
</center>
<?php include("bmenu.php"); ?>
</body>
