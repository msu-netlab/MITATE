<?php include('header.php'); ?>
	<div style="font-size: 18;text-align: justify;">
	<h3 style="text-decoration:underline">Signup for MITATE:</h3>
	<br />
	<form action="" method="POST">
	<div><div>First name:</div><div><input type="text" placeholder="First name" id="fname" name="fname" /></div></div>
	<div><div>Last Name:</div><div><input type="text" placeholder="Last name" id="lname" name="lname" /></div></div>
	<div><div>Username:</div><div><input type="text" placeholder="Username" id="username" name="username" /></div></div>
	<div><div>Email:</div><div><input type="text" placeholder="Valid Email" id="email" name="email" /></div></div>
	<div><div>Password:</div><div><input type="password" placeholder="Password" id="password" name="password" /></div></div>
	<div><div><input type="submit" value="Create account" /></div></div>
	</form>
	</div>
<?php include('footer.php'); ?>
<?php
if(isset($_POST['fname']) || isset($_POST['lname']) || isset($_POST['email']) || isset($_POST['username']) || isset($_POST['password'])) {
	$con = mysql_connect("localhost","mitate","Database4Mitate");
	if (!$con) {die('Could not connect: ' . mysql_error());}
	mysql_select_db("mitate", $con);
	$result = mysql_query("SELECT * FROM userinfo");
	$k=0;
	while($row = mysql_fetch_array($result)) {
		if( $row['username'] == $_POST["username"] || $row['email'] == $_POST["email"])
			$k=1;
	}
	if($k == 0) {
		$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[password], MCRYPT_MODE_CBC, md5(md5("mitate"))));
		$current_date = date("Y-m-d");
		$sql="INSERT INTO userinfo (fname, lname, username, password, email, datecreated, status) VALUES ('$_POST[fname]','$_POST[lname]','$_POST[username]','$encrypted_password','$_POST[email]', '$current_date', 0)";
		if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
		mkdir("user_accounts/$_POST[username]", 0777);
		mkdir("user_accounts/$_POST[username]/experiments", 0777);
		mkdir("user_accounts/$_POST[username]/validate", 0777);
		mkdir("user_accounts/$_POST[username]/countcredit", 0777);
		
		$verification_key = md5(uniqid($_POST[username], true));
		$sql="insert into user_verification (username, verification_key, if_used) values ('$_POST[username]', '$verification_key', 0)";
		if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
		
		$msg="Hi $_POST[fname],<br /><br />Please click on the verification link below to complete your registration with MITATE.<br /><br />http://mitate.cs.montana.edu/user_verification.php?key=$verification_key <br /><br />Thank you, <br /><br />Team MITATE";
		mail($_POST[email], "Account Verification", $msg, "From: MITATE <mitate@cs.montana.edu>\r\n MIME-Version: 1.0\r\n Content-type:text/html;charset=UTF-8 \r\n");
		printf("<script>alert('A verification email has been sent to the email address you provided. Please click on the verification link in the email to complete the registration process.')</script>");
	}
	else {
		printf("<script>alert('The username/email that you have chosen already exists. Please try another one.')</script>");
	} 
	mysql_close($con);
}
?>
