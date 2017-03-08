<?php
if ($_POST['fname'] != '' && $_POST['lname'] != '' && $_POST['email'] != '' && $_POST['username'] != '' && $_POST['password'] != '') {
    libxml_use_internal_errors(true);
    $xml = simplexml_load_file("config.xml");
    $dbhostname = $xml->databaseConnection->serverAddress;
    $dbusername = $xml->databaseConnection->user;
    $dbpassword = $xml->databaseConnection->password;
    $dbschemaname = $xml->databaseConnection->name;
    $passwordEncryptionKey = $xml->database->passwordEncryptionKey;
    $con = mysql_connect($dbhostname, $dbusername, $dbpassword);
    if (!$con) {
        die('Could not connect: ' . mysql_error());
    }
    mysql_select_db($dbschemaname, $con);
    $result = mysql_query("SELECT * FROM userinfo");
    $k = 0;
    while ($row = mysql_fetch_array($result)) {
        if ($row['username'] == $_POST["username"] || $row['email'] == $_POST["email"])
            $k = 1;
    }
    if ($k == 0) {
        $encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $_POST[password], MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
        $current_date = date("Y-m-d");
        $sql = "INSERT INTO userinfo (fname, lname, username, password, email, datecreated, status) VALUES ('$_POST[fname]','$_POST[lname]','$_POST[username]','$encrypted_password','$_POST[email]', '$current_date', 0)";
        if (!mysql_query($sql, $con)) {
            die('Error: ' . mysql_error());
        }
        mkdir("user_accounts/$_POST[username]", 0777);
        mkdir("user_accounts/$_POST[username]/experiments", 0777);
        mkdir("user_accounts/$_POST[username]/validate", 0777);
        mkdir("user_accounts/$_POST[username]/countcredit", 0777);

        $verification_key = md5(uniqid($_POST[username], true));
        $sql = "insert into user_verification (username, verification_key, if_used) values ('$_POST[username]', '$verification_key', 0)";
        if (!mysql_query($sql, $con)) {
            die('Error: ' . mysql_error());
        }

        $msg = "Hi $_POST[fname],<br /><br />Please click on the verification link below to complete your registration with MITATE.<br /><br />http://mitate.cs.montana.edu/user_verification.php?key=$verification_key <br /><br />Thank you, <br /><br />Team MITATE";

        $headers = "From: MITATE <mitate@cs.montana.edu>\r\n";
        $headers .= "MIME-Version: 1.0\r\n";
        $headers .= "Content-Type: text/html; charset=ISO-8859-1\r\n";

        mail($_POST[email], "MITATE Account Verification", $msg, $headers);
        printf("<script>alert('A verification email has been sent to the email address you provided. Please click on the verification link in the email to complete the registration process.')</script>");
    } else {
        printf("<script>alert('The username or email that you have chosen already exists. Please try another one.')</script>");
    }
    mysql_close($con);
}
?>
<?php include('header.php'); ?>
<script type="text/javascript">
    function validateSignupForm() {
        var form = document.usersignup;
        if (form.fname.value == "" || form.lname.value == "" || form.username.value == "" || form.password.value == "" || form.email.value == "") {
            alert("Please enter all the input fields.");
            return false;
        }
        else {
            var atpos = form.email.value.indexOf("@");
            var dotpos = form.email.value.lastIndexOf(".");
            if (atpos < 1 || dotpos < atpos + 2 || dotpos + 2 >= x.length) {
                alert("Please enter a valid e-mail address. Note that, we will send you a verification link on this email address to complete your registration with MITATE.");
                return false;
            }
        }
    }

</script>
<div style="font-size: 18;text-align: justify;">
    <h3 style="text-decoration:underline">Signup for MITATE:</h3>
    <br/>
    <form action="" method="POST" name="usersignup" onsubmit="return validateSignupForm();">
        <div>
            <div>First name:</div>
            <div><input type="text" placeholder="First name" id="fname" name="fname"/></div>
        </div>
        <div>
            <div>Last Name:</div>
            <div><input type="text" placeholder="Last name" id="lname" name="lname"/></div>
        </div>
        <div>
            <div>Username:</div>
            <div><input type="text" placeholder="Username" id="username" name="username"/></div>
        </div>
        <div>
            <div>Email:</div>
            <div><input type="text" placeholder="Valid Email" id="email" name="email"/></div>
        </div>
        <div>
            <div>Password:</div>
            <div><input type="password" placeholder="Password" id="password" name="password"/></div>
        </div>
        <div>
            <div><input type="submit" value="Create account"/></div>
        </div>
    </form>
</div>
<?php include('footer.php'); ?>
