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
  $sql="INSERT INTO userinfo (fname, lname, username, password, email) VALUES ('$_POST[fname]','$_POST[lname]','$_POST[username]','$encrypted_password','$_POST[email]')";

if (!mysql_query($sql,$con))
  {
  die('Error: ' . mysql_error());
  }
  else
  {
mkdir("user_accounts/$_POST[username]", 0777);
  $msg="Congratulations! You have been successfully registered with MITATE.";
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


