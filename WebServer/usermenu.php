<?php
if (!isset($_COOKIE["username"]))
  printf("<script>location.href = 'index.php'</script>");
?>
<html>
<head>
    <title></title>
    <style type="text/css">
        .style1
        {
            width: 632px;
        }
    </style>
<script>

  window.history.forward()
  
</script>
</head>
  <body bgcolor="#FFFFFF">

  <table style="width:98%;" bgcolor="#FFFFFF" align="center">
        <tr>
          <td align="left" class="style1" valign="middle" style="font-family: Calibri;">

  <font size="4">MITATE: Mobile Internet Testbed for Application Traffic Experimentation</font>
          </td>
<td align="center" style="font-family: Calibri;"><?php 
if (isset($_COOKIE["username"]))
  echo "Welcome " . $_COOKIE["username"];
?>
</td>
                  </tr>
        </table>
    <hr />
    
<table align="right" style="width: 61%;">
        <tr>
<td align="right" class="style9" 
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="welcome.php" style="text-decoration:none; color:#000000">Welcome</a></td>
 <td align="right" class="style9" 
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="main.php" style="text-decoration:none; color:#000000">Upload Configuration File</a></td>
            <td align="right" class="style9" 
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="pending.php" style="text-decoration:none; color:#000000">Pending Tests</a></td>
            <td align="right" class="style7" 
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="complete.php" style="text-decoration:none; color:#000000">Completed Tests</a></td>
 <td align="right" class="style7"
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="Settings.php" style="text-decoration:none; color:#000000">Mobile Settings</a></td>            
<td align="right" class="style10" 
                style="color: #FFFFFF; font-family: Calibri; font-size: small">
                <a href="logout.php" style="text-decoration:none; color:#000000">Logout</a></td>
				        </tr>
    </table>
<br />
<hr style="width:100%;" />
<br />

  </body>
</html>
