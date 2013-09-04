<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Login | MITATE</title>
    <style type="text/css">
        .style2
        {
            height: 15px;
        }
        .style3
        {
            width: 161px;
        }
        .style4
        {
            width: 167px;
        }
        .style5
        {
            width: 133px;
        }
        #susername
        {
            height: 35px;
            width: 210px;
        }
        #spassword
        {
            width: 210px;
            height: 35px;
        }
    </style>
    <script language="javascript">
        function validateForm1() {
            var form = document.usersignin;
            if (form.susername.value == "") {
                alert("Please Enter your username");
                form.susername.focus();
                return false;
            }
            if (form.spassword.value == "") {
                alert("Please Enter your password");
                form.spassword.focus();
                return false;
            }
        }
    </script>
</head>
<body bgcolor="#FFFFFF" style="font-family: Calibri;">
<?php
  if($_POST["susername"]!="" || $_POST["spassword"]!="")
  {
   $con = mysql_connect("localhost","root","root");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
  $result = mysql_query("SELECT * FROM userinfo");
  $k=0;
  while(($row = mysql_fetch_array($result)) && $k==0 )
  {
  if(($row['username'] == $_POST["susername"] || $row['email'] == $_POST["susername"]) && $row['password'] == $_POST["spassword"])
  {
  $k=1;
 
  }
  }
  if($k==0)
  {
  printf("<script>location.href = 'loginfail.php'</script>");
   }
  elseif($k==1)
  {
$expire=time()+60*60*24*30;
setcookie("username", "$_POST[susername]", $expire, '/');
$_COOKIE['username'] = "$_POST[susername]";
printf("<script>location.href = 'welcome.php'</script>");
}

  }
  
  ?>

<?php include('umenus.php'); ?>
    <br />

    <table align="center" style="border: thin double #C0C0C0; width: 52%;">
        <tr>
            <td>
                &nbsp;</td>
            <td>
                &nbsp;</td>
            <td>
                &nbsp;</td>
        </tr>
        <tr>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                &nbsp;</td>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                MITATE Login</td>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                &nbsp;</td>
        </tr>
        <tr>
            <td style="color:#000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                &nbsp;</td>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                <hr align="left" style="width: 435px; height: -12px" />
            </td>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                &nbsp;</td>
        </tr>
        <tr>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                &nbsp;</td>
            <td style="border: thin solid #FF0000; color: #000000; font-size: medium; font-weight: lighter; font-style: normal; font-family: Calibri; background-color: #FFCCFF;">
            
                <h3>Incorrect Account Credentials!</h3>
              
                The Login details you entered does not belong to any account. You may try 
                clearing your browser&#39;s cache and cookies.<br />
                <br />
                You can login using any email or username associated with your account. Make 
                sure that it is typed correctly.<br />
&nbsp;</td>
            <td>
                &nbsp;</td>
        </tr>
        <tr>
            <td style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;" 
                class="style2">
                </td>
            <td class="style2">
            
             <form action="" method="post" name="usersignin" >


      <table style="width:100%;" bgcolor="white">
        <tr>
          
          <td class="style5">
              &nbsp;</td>
          <td class="style4">
              &nbsp;</td>
        </tr>
        <tr>
          <td class="style5" align="right" style="color: #000000; font-family: Calibri;">
            &nbsp;Email:</td>
          <td class="style4">
            <input id="susername" type="text" name="susername" /></td>
        </tr>
        <tr>
          <td class="style5" align="right" style="color: #000000; font-family: Calibri;">
              Password:</td>
          <td class="style4">
            <input id="spassword" type="password" name="spassword" /></td>
        </tr>
        <tr>
          <td class="style5" align="right" style="color: #000000; font-family: Calibri;">
              &nbsp;</td>
          <td class="style4" style="color: #000000; font-family: Calibri;" valign="middle">
            <input id="Submit1" type="submit" value="Login"
                
                  style="background-color: #666666; color: #000000; font-family: Calibri; width: 64px; font-size: large;" 
                  onclick="return validateForm1()" /> or <a href="index.php" style="text-decoration:none; color:#000000"> Sign Up for MITATE</a></td>
        </tr>
        <tr>
          <td class="style5">
            &nbsp;
          </td>
          <td class="style4" style="color: #C0C0C0; font-family: Calibri;">
            <a href="forgotpass.php" style="text-decoration:none; color:#000000" >Forgot your password?</a>
          </td>
        </tr>
      </table>
    </form>

                </td>
            <td class="style2">
                </td>
        </tr>
    </table>
    <?php include("bmenu.php") ?>
</body>
</html>
