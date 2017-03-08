<?php
if (isset($_POST["username"]) && isset($_POST["email"])) {
    libxml_use_internal_errors(true);
    $xml = simplexml_load_file("config.xml");
    list($bigQuery, $dataset) = require 'get_bq_connection.php';
    if (!$bigQuery) {
        die('Could not connect to BigQuery');
    }
    //$queryResults = $bigQuery->runQuery("SELECT * FROM [MITATE.userinfo] WHERE email='{$email}' OR username='{$username}'");
    $queryResults = $bigQuery->runQuery("SELECT * FROM MITATE.userinfo");

    // Wait for the query to complete before continuing
    $isComplete = $queryResults->isComplete();
    while (!$isComplete) {
        sleep(1);
        $queryResults->reload();
        $isComplete = $queryResults->isComplete();
    }

    $found = False;
    foreach ($queryResults->rows() as $row) {
        if ($row['username'] == $_POST["username"] || $row['email'] == $_POST["email"]) {
            $found = True;
            $decrypted_password = rtrim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), base64_decode($row[password]), MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))), "\0");
            $headers = "From: MITATE <mitate@cs.montana.edu>\r\n";
            $headers .= "MIME-Version: 1.0\r\n";
            $headers .= "Content-Type: text/html; charset=ISO-8859-1\r\n";
            $msg = "Hi " . $row['fname'] . ", <br /><br />This is a password recovery email for your account with MITATE as requested by you. Your account details are as follows:<br /><br />  Username: " . $row['username'] . "<br />Password: " . $decrypted_password . "<br />Email: " . $row['email'] . "<br /><br />Thank you<br />The Team MITATE";
            mail($row['email'], "Password Recovery for your MITATE Account", $msg, $headers);
            printf("<script>alert('An email has been sent to the email address you registered with this account.')</script>");
        }
    }
    if ($found == False) {
        printf("<script>alert('The detail you entered does not belong to any account. Make sure you typed correctly.')</script>");
    }
}
?>
<?php include("header.php"); ?>
<script type="text/javascript">
    function validatePasswordRecoveryForm() {
        var form = document.userPasswordRecoveryForm;
        if (form.username.value == "" && form.email.value == "") {
            alert("Please enter one of the input fields.");
            return false;
        }
    }
</script>
<form action="" method="post" name="userPasswordRecoveryForm" onsubmit="return validatePasswordRecoveryForm();">
    <table align="left" style="width: 62%; color: #000000; font-family: Calibri; font-size: large;">
        <tr>
            <td align="left" colspan="2"
                style="color: #000000; font-size: xx-large; font-weight: bold; font-style: italic; font-family: Calibri;">
                Identify Your Account
            </td>
        </tr>
        <tr>
            <td align="left" colspan="2">
                Before we can reset your password, you need to enter the information below to
                help identify your account:
            </td>
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
                Enter your username
            </td>
        </tr>
        <tr>
            <td class="style2">
                &nbsp;</td>
            <td align="left">
                <input id="username" style="font-family: Calibri; font-size: large; height: 30px; width: 210px;"
                       type="text" name="username"/></td>
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
                OR
            </td>
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
                Enter your email address
            </td>
        </tr>
        <tr>
            <td align="right" class="style2">
                &nbsp;</td>
            <td align="left">
                <input id="email" style="font-family: Calibri; font-size: large; height: 30px; width: 210px;"
                       type="text" name="email"/></td>
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
                       type="submit" value="Identify"/>
        </tr>
    </table>
</form>
<?php include("footer.php"); ?>
</body>
</html>
