<?php
if (isset($_COOKIE["username"]))
  printf("<script>location.href = 'welcome.php'</script>");

  if($_POST["susername"]!="" || $_POST["spassword"]!="")
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

<html>
  <head>
    <style type="text/css">
      .style3
      {
      width: 750px;
      }
      .style4
      {
      width: 144px;
      }
      .style5
      {
      width: 140px;
      }
    </style>
    <script language="javascript">
  window.history.forward()
  
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
  <body>
        
    <form action="" method="post" name="usersignin" >


      <table style="width:100%;" bgcolor="white">
        <tr>
          <td align="left" class="style3" rowspan="3" valign="middle" style="font-family: Calibri;">
           <font size="4">MITATE: Mobile Internet Testbed for Application Traffic Experimentation</font>
          </td>
          <td class="style5" style="color: #000000; font-family: Calibri;">
            Username
          </td>
          <td class="style4" style="color: #000000; font-family: Calibri;">
            Password
          </td>
          <td>
            &nbsp;
          </td>
        </tr>
        <tr>
          <td class="style5">
            <input id="susername" type="text" name="susername" />
          </td>
          <td class="style4">
            <input id="spassword" type="password" name="spassword" />
          </td>
          <td>
            <input id="Submit1" type="submit" value="Login"
                style="background-color: #C0C0C0; color: #000000; font-family: Calibri;" onclick="return validateForm1()" />
          </td>

        </tr>
        <tr>
          <td class="style5">
            &nbsp;
          </td>
          <td class="style4" style="color: #000000; font-family: Calibri;">
            <a href="forgotpass.php" style="text-decoration:none; color:#000000" >Forgot your password?</a>
          </td>
          <td>
            &nbsp;
          </td>
        </tr>
      </table>
    </form>

    <hr />
    <br />   
  </body>
</html>
