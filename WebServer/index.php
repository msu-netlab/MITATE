<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Welcome to MITATE - Log In, Sign Up, Learn More
</title>
    <style type="text/css">
        
        .style6
        {
            width: 705px;
        }
        #Text3
        {
            height: 35px;
            width: 210px;
        }
        #Text4
        {
            height: 35px;
            width: 210px;
        }
        #Text5
        {
            height: 35px;
            width: 210px;
        }
        #Text6
        {
            height: 35px;
            width: 210px;
        }
        #Text7
        {
            height: 35px;
            width: 210px;
        }
        #Text8
        {
            height: 35px;
            width: 210px;
        }
        #Submit2
        {
            height: 44px;
            width: 103px;
        }
        
    </style>

    <script language="javascript">
       

        function validateForm() {
            var form = document.UserRegForm;
           

            if (form.fname.value == "") {
                alert("Please Enter First Name");
                form.fname.focus();
                return false;
            }
            if (form.lname.value == "") {
                alert("Please Enter Last Name");
                form.lname.focus();
                return false;
            }

            var x = document.forms["UserRegForm"]["email"].value
            var atpos = x.indexOf("@");
            var dotpos = x.lastIndexOf(".");

            if (form.email.value == "") {
                alert("Please Enter your email id");
                form.email.focus();
                return false;
            }
            if (form.email.value.charAt(0) == " ") {
                alert("Please Provide Valid EmailId");
                form.email.value = "";
                form.email.focus();
                return false;
            }
            if (atpos < 1 || dotpos < atpos + 2 || dotpos + 2 >= x.length) {
                alert("Not a valid e-mail address");
                form.email.focus();
                return false;
            }
            if (form.username.value == "") {
                alert("Please Enter your desired username");
                form.username.focus();
                return false;
            }
            if (form.pass1.value == "") {
                alert("Please Enter password");
                form.pass1.focus();
                return false;
            }
            if (form.pass2.value == "") {
                alert("Please Enter confirm password");
                form.pass2.focus();
                return false;
            }
            if (form.pass1.value != form.pass2.value) {
                alert("Passwords do not match");
                form.pass1.value = ""
                form.pass2.value=""
                form.pass1.focus();
                return false;
            }
            
        }
</script>

</head>
<body bgcolor="white">
<?php include("umenu.php"); ?>
   
    
    <form action="signup.php" method="post" name="UserRegForm" >
    <table style="width:100%;">
        <tr>
            <td rowspan="10" class="style6" align="center" valign="middle">
               

				</td>
            <td colspan="2" 
                style="color: #000000; font-size: x-large; font-weight: bold; font-style: italic; font-family: Calibri;" 
                align="center">
                Sign Up</td>
        </tr>
        <tr>
            <td style="color:  #000000">
                &nbsp;</td>
            <td style="color:  #000000">
                &nbsp;</td>
        </tr>
        <tr>
            <td style="width: 200px; color:  #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                First Name:</td>
            <td style="color:  #000000">
                <input id="fname" type="text" 
                    
                    
                    style="font-family: Calibri; font-size: large; color: #000000; height: 35px; width: 210px;" 
                    name="fname" /></td>
        </tr>
        <tr>
            <td style="color:  #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                Last Name:</td>
            <td style="color: #000000">
                <input id="lname" type="text" 
                    
                    
                    style="font-family: Calibri; font-size: large; color: #000000; width: 210px; height: 35px;" 
                    name="lname" /></td>
        </tr>
        
        <tr>
            <td style="color: #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                Username:</td>
            <td style="color: #000000">
                <input id="username" type="text" 
                    
                    
                    style="font-family: Calibri; font-size: large; color: #000000; width: 210px; height: 35px;" 
                    name="username" /></td>
        </tr>
        <tr>
            <td style="color: #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                Password:</td>
            <td style="color:#000000">
                <input id="pass1" type="password"                
                    
                    style="font-family: Calibri; font-size: large; color: #000000; width: 210px; height: 35px;" 
                    name="pass1"  /></td>
        </tr>
        <tr>
            <td style="color:#000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                Confirm Password:</td>
            <td style="color: #000000">
                <input id="pass2" type="password" 
                    
                    
                    style="font-family: Calibri; font-size: large; color: #000000; width: 210px; height: 35px;" 
                    name="pass2" /></td>
        <tr>
            <td style="color:  #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                Your Email:</td>
            <td style="color:  #000000">
                <input id="email" type="text" 
                    
                    
                    style="font-family: Calibri; font-size: large; color: #000000; width: 210px; height: 35px;" 
                    name="email" /></td>
        </tr>
		
        <tr>
            <td style="color: #000000; font-size: large; font-family: Calibri;" 
                align="right" valign="middle">
                &nbsp;</td>
            <td style="color: #FFFFFF">
                <input id="Submit2" 
                    style="font-family: Calibri; color: #FFFFFF; background-color: #999999; font-size: large;" 
                    type="submit" value="Sign Up" onclick="return validateForm()" /></td>
        </tr>
    </table>
    </form>
   
        <?php include("bmenu.php"); ?>
    <br />   
</body>
</html>
