<html>
  <head>
    <title>Validate - MITATE</title>
  </head>
  
  <?php
  if(!empty($_POST) && ($_POST['fname'] == "" || $_POST['lname'] == "" || $_POST['email'] == "" || $_POST['username'] == "" || $_POST['pass1'] == ""))
    {
  printf("<script>location.href = 'index.php'</script>");
      }
	else
    {
	include("umenus.php"); ?>
   <body bgcolor="white" style="font-family: Calibri;">
      <table align="center" style="width: 74%;">
          <tr>
              <td align="center" colspan="3" 
                  style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                  
    <?php
    
     $con = mysql_connect("localhost","mitate","Database4Mitate");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
$result = mysql_query("SELECT * FROM userinfo");

  $k=0;
while($row = mysql_fetch_array($result))
  {
 
  if( $row['username'] == $_POST["username"])
  {
  $k=1;
  }
  }

   if($k == 0)
  {
  $encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[pass1], MCRYPT_MODE_CBC, md5(md5("mitate"))));
  $current_date = date("Y-m-d");
  $sql="INSERT INTO userinfo (fname, lname, username, password, email, datecreated) VALUES ('$_POST[fname]','$_POST[lname]','$_POST[username]','$encrypted_password','$_POST[email]', '$current_date')";

if (!mysql_query($sql,$con))
  {
  die('Error: ' . mysql_error());
  }
  else
  {
mkdir("user_accounts/$_POST[username]", 0777);
mkdir("user_accounts/$_POST[username]/experiments", 0777);
mkdir("user_accounts/$_POST[username]/validate", 0777);
mkdir("user_accounts/$_POST[username]/countcredit", 0777);
$msg="Congratulations! You have been successfully registered with MITATE.";
$start_value = 1000000000;
$credit_id = $start_value;
$get_credit_id_counts = mysql_query("SELECT count(*) as count, max(credit_id) as maxval from usercredits");
while($get_credit_id_count = mysql_fetch_assoc($get_credit_id_counts)) {
	if($get_credit_id_count[count] > 0)
		$credit_id = $get_credit_id_count[maxval] + 1;
}
$sql_store_credits ="INSERT INTO usercredits (credit_id, username, available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits) VALUES($credit_id, '$_POST[username]', 200, 0, 500, 0)";
if (!mysql_query($sql_store_credits, $con)) {die('Error: ' . mysql_error());}			
 mail($_POST[email], "Account Created - MNEP", $msg);
echo "You have been successfully registered with MITATE. Please <a href=index.php>Sign In</a> to proceed";
echo "<br />";
}
}
else
{
echo "Sorry! The username that you have chosen already exists. Please try another one.";
}
 
mysql_close($con);

}

?>
                  
                  
                  </td>
          </tr>
         
      </table>

    <?php include("bmenu.php"); ?>
  </body>
</html>


