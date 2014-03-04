<?php session_start(); ?>
<?php
if(isset($_POST['userid']) && isset($_POST['password'])) {
	$con = mysql_connect("localhost","mitate","Database4Mitate");
	if (!$con) {die('Could not connect: ' . mysql_error());}
	mysql_select_db("mitate", $con);
	$result = mysql_query("SELECT * FROM userinfo");
	$k=0;
	$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
	while($row = mysql_fetch_array($result)) {
		if(($row['username'] == $_POST["userid"] || $row['email'] == $_POST["userid"]) && $row['password'] == $encrypted_password && $row['status'] == 1)
			$k=1;
	}
	if($k == 0) {
		printf("<script>alert('Invalid login credentials.')</script>");
	}
	else {
		if(($_POST["userid"] == "mitateuser" && $_POST[password] == "mitatepassword") || ($_POST["userid"] == "mitate@gmail.com" && $_POST[password] == "mitatepassword") || ($_POST["userid"] == "mlabuser" && $_POST[password] == "mlabpassword") || ($_POST["userid"] == "utkarsh.090@gmail.com" && $_POST[password] == "mlabpassword")) {
			$_SESSION['mitateLoggedInUser'] = $_POST["userid"];
			printf("<script>location.href = 'mitate_downloads.php'</script>");
		}
		else {
			printf("<script>alert('This page can only be viewd by the administrator. Please login as administrator or contact us to get access. Thank you')</script>");
			printf("<script>location.href = 'index.php'</script>");
		}
	} 
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
	<h3 style="text-decoration:underline">Sign in for MITATE:</h3>
	<br />
	<form action="" method="POST" name="userSigninForm" onsubmit="return validateSigninForm();">
	<div><div>Username/Email:</div><div><input type="text" placeholder="Username/Email" id="userid" name="userid" /></div></div>
	<div><div>Password:</div><div><input type="password" placeholder="Password" id="password" name="password" /></div></div>
	<div><div><input type="submit" value="Sign in" /></div></div>
	<br />
	<div><div><a href="forgotpass.php">Forgot Password?</a></div></div>
	</form>
	</div>
<?php include('footer.php'); ?>