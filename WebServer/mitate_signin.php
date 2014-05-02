<?php session_start(); ?>
<?php
$xml = simplexml_load_file("config.xml");
$webSitename = $xml->webServer->webSiteName;
if(isset($_POST['userid']) && isset($_POST['password'])) {
	$dbhostname = $xml->databaseConnection->serverAddress;
	$dbusername = $xml->databaseConnection->user;
	$dbpassword = $xml->databaseConnection->password;
	$dbschemaname = $xml->databaseConnection->name;
	$passwordEncryptionKey = $xml->database->passwordEncryptionKey;
	$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
	if (!$con) {
		die('Website down for maintenance. We will be live soon.');
	}
	mysql_select_db($dbschemaname, $con);
	$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $_POST[password], MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
	$result = mysql_query("SELECT * FROM userinfo where username = '$_POST[userid]' and password = '$encrypted_password'");
	$isUserValid=0;
	$isUserAdmin = 0;
	$rowCount = 0;
	while($row = mysql_fetch_array($result)) {
			$isUserValid = $row['status'];
			$isUserAdmin = $row['isAdmin'];
			$rowCount = $rowCount + 1;
	}
	if ($rowCount > 0) {
		if($isUserValid == 0) {
			printf("<script>alert('Your account registration is not complete yet. Please click on the verification link in the email that we sent you during signup.')</script>");
		}
		else if ($isUserValid == 1 && $isUserAdmin == 0) {
			printf("<script>alert('This page can only be viewd by the administrator. Please login as administrator or contact us to get access. Thank you')</script>");
		}
		else if ($isUserValid == 1 && $isUserAdmin == 1) {
			$_SESSION['mitateLoggedInUser'] = $_POST["userid"];
			printf("<script>location.href = 'mitate_downloads.php'</script>");
		} 
	}
	else 
		printf("<script>alert('Invalid account credentials.')</script>");
	mysql_close($con);
}
?>
<?php include('header.php'); ?>
<script type="text/javascript">
function validateSigninForm() {
    var form = document.userSigninForm;
    if (form.userid.value == "" || form.password.value == "") {
		alert("Please enter all the input fields.");
		return false;
    }
}
</script>
	<div style="font-size: 18;text-align: justify;">
	<h3 style="text-decoration:underline">Sign in for <?php echo $webSitename; ?>:</h3>
	<br />
	<form action="" method="POST" name="userSigninForm" onsubmit="return validateSigninForm();">
	<div><div>Username:</div><div><input type="text" placeholder="Username/Email" id="userid" name="userid" /></div></div>
	<div><div>Password:</div><div><input type="password" placeholder="Password" id="password" name="password" /></div></div>
	<div><div><input type="submit" value="Sign in" /></div></div>
	<br />
	<div><div><a href="forgotpass.php">Forgot Password?</a></div></div>
	</form>
	</div>
<?php include('footer.php'); ?>