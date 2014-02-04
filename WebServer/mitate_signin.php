<?php 
	session_start();
	include('header.php'); ?>
	<div style="color: white;font-size: 22;text-align: justify;">
	<h3 style="text-decoration:underline">Sign in for MITATE:</h3>
	<br />
	<form action="" method="POST">
	<div><div>Username/Email:</div><div><input type="text" placeholder="Username/Email" id="userid" name="userid" /></div></div>
	<div><div>Password:</div><div><input type="password" placeholder="Password" id="password" name="password" /></div></div>
	<div><div><input type="submit" value="Sign in" /></div></div>
	</form>
	</div>
<?php include('footer.php'); ?>
<?php
if(isset($_POST['userid']) || isset($_POST['password'])) {
	$con = mysql_connect("localhost","mitate","Database4Mitate");
	if (!$con) {die('Could not connect: ' . mysql_error());}
	mysql_select_db("mitate", $con);
	$result = mysql_query("SELECT * FROM userinfo");
	$k=0;
	$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
	while($row = mysql_fetch_array($result)) {
		if(($row['username'] == $_POST["userid"] || $row['email'] == $_POST["userid"]) && $row['password'] == $encrypted_password)
			$k=1;
	}
	if($k == 0) {
		printf("<script>alert('Invalid login credentials.')</script>");
	}
	else {
		if($_POST[password] == "mitatepassword") {
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
