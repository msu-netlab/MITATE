<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Forgot password - MITATE</title>
    <style type="text/css">
        #Text1
        {
            height: 33px;
            width: 220px;
        }
        #Text2
        {
            height: 33px;
            width: 220px;
        }
        #Submit3
        {
            width: 93px;
            height: 36px;
        }
        #Submit4
        {
            width: 93px;
            height: 36px;
        }
        .style2
        {
            width: 133px;
        }
    </style>
     <script language="javascript">
                 
         function clearForm() {
             var form = document.UserfpForm;
             form.Text1.value = ""
             form.Text2.value = ""
             return true;
         }

         function checkForm() {
             var form = document.UserfpForm;
             var x = document.forms["UserfpForm"]["lemail"].value
             var atpos = x.indexOf("@");
             var dotpos = x.lastIndexOf(".");
            
             if (form.lusername.value != "" && form.lemail.value != "") {
                 alert("Please enter only one of your account details");
                 form.lusername.focus();
                 return false;
             }
             if (form.lemail.value.charAt(0) == " ") {
                 alert("Please Provide Valid EmailId");
                 form.lemail.value = "";
                 form.lemail.focus();
                 return false;
             }
             if (form.lemail.value != "" && (atpos < 1 || dotpos < atpos + 2 || dotpos + 2 >= x.length)) {
                 alert("Not a valid e-mail address");
                 form.lemail.focus();
                 return false;
             }
             if (form.lusername.value == "" && form.lemail.value == "") {
                 alert("Please enter any one of your account details");
                 form.lusername.focus();
                 return false;
             }
         }

            </script>
</head>
<body bgcolor="white" style="font-family: Calibri;">

<?php include("umenu.php"); ?>
 <form action="" method="post" name="UserfpForm" >
    <table align="center" 
        
        
        
        style="width: 62%; color: #000000; font-family: Calibri; font-size: large;">
        <tr>
            <td align="left"  colspan="2" 
                
                style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                Identify Your Account</td>
        </tr>
        <tr>
            <td align="left" colspan="2" 
                
                
                style="color: #000000; font-family: Calibri; background-color: #FFCCFF; border: thin solid #FF0000; font-size: small;">
                
  <?php
  if($_POST["lusername"]!="" || $_POST["lemail"]!="")
  {
    $con = mysql_connect("localhost","mitate","Database4Mitate");
  if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }
  mysql_select_db("mitate", $con);
  $result = mysql_query("SELECT * FROM userinfo");
  $k=0;
  while(($row = mysql_fetch_array($result)) && $k==0 )
  {
  if($row['username'] == $_POST["lusername"] || $row['email'] == $_POST["lemail"])
  {
  $k=1;
  $decrypted_password = rtrim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), base64_decode($row[pass]), MCRYPT_MODE_CBC, md5(md5("mitate"))), "\0");

  $msg="This is a recovery email for your account at MNEP System as requested by you. Your Account details are as follows:  Username - " . $row['username'] . ", Password - " . $decrypted_password . ", Email - " . $row['email'] ;
  mail($row['email'], "Password Recovery - NMEP", $msg);
  printf("<h4 >Email Sent</h4>");
   echo "An email has been sent to the email address you registered with the detail you provided." ;
   }
  }
  if($k==0)
  {
  printf("<h4 >We Couldn't Find Your Account</h4>");
  echo "The detail you entered does not belong to any account. Make sure that it is typed correctly. You may also try again using any email or username number associated with your account.";
   }
  
  }
  ?>
                
                </td>
        </tr>
        <tr>
            <td align="left"  colspan="2">
                Before we can reset your password, you need to enter the information below to 
                help identify your account:</td>
        </tr>
        <tr>
            <td align="right" class="style2">
                &nbsp;</td>
            <td>
                &nbsp;</td>
        </tr>
        <tr>
            <td align="right">
                </td>
            <td align="left">
                Enter your login username</td>
        </tr>
        <tr>
            <td class="style2">
                &nbsp;</td>
            <td align="left">
                <input id="lusername" style="font-family: Calibri; font-size: large; height: 30px; width: 210px;" 
                    type="text" name="lusername" /></td>
        </tr>
        <tr>
            <td class="style2">
                &nbsp;</td>
            <td align="left">
                &nbsp;</td>
        </tr>
        <tr>
            <td class="style2">
                &nbsp;</td>
            <td align="left" 
                style="font-size: large; font-weight: bold; font-style: italic">
                OR</td>
        </tr>
        <tr>
            <td class="style2">
                &nbsp;</td>
            <td>
                &nbsp;</td>
        </tr>
        <tr>
            <td align="right">
               </td>
            <td align="left">
                Enter your email address</td>
        </tr>
        <tr>
            <td align="right" class="style2">
                &nbsp;</td>
            <td align="left">
                <input id="lemail" style="font-family: Calibri; font-size: large; height: 30px; width: 210px;" 
                    type="text" name="lemail" /></td>
        </tr>
        <tr>
            <td align="right" class="style2">
                &nbsp;</td>
            <td align="left">
                &nbsp;</td>
        </tr>
        <tr>
            <td align="right" class="style2">
                &nbsp;</td>
            <td align="left">
                <input id="submit3" 
                    style="font-family: Calibri; font-size: large; color: #FFFFFF; background-color: #666666; width: 96px; height: 35px;" 
                    type="submit" value="Identify" onclick="return checkForm()" /><input id="submit4" 
                    style="font-family: Calibri; font-size: large; color: #FFFFFF; background-color: #666666; width: 96px; height: 35px;" 
                    type="button" value="Clear" onclick="return clearForm()" /></td>
        </tr>
    </table>
    </form>
    <?php include("bmenu.php"); ?>
</body>
</html>
