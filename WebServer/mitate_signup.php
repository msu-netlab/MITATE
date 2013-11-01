<?php include('header.php'); ?>
	<div style="color: white;font-size: 22;text-align: justify;">
	<h3 style="text-decoration:underline">Signup for MITATE:</h3>
	<br />
	<form action="" method="POST">
	<div><div>First name:</div><div><input type="text" placeholder="First name" id="fname" name="fname" /></div></div>
	<div><div>Last Name:</div><div><input type="text" placeholder="Last name" id="lname" name="lname" /></div></div>
	<div><div>Username:</div><div><input type="text" placeholder="Username" id="username" name="username" /></div></div>
	<div><div>Email:</div><div><input type="text" placeholder="Valid Email" id="email" name="email" /></div></div>
	<div><div>Password:</div><div><input type="text" placeholder="Password" id="password" name="password" /></div></div>
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
		$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $_POST[pass1], MCRYPT_MODE_CBC, md5(md5("mitate"))));
		$current_date = date("Y-m-d");
		$sql="INSERT INTO userinfo (fname, lname, username, password, email, datecreated) VALUES ('$_POST[fname]','$_POST[lname]','$_POST[username]','$encrypted_password','$_POST[email]', '$current_date')";
		if (!mysql_query($sql,$con)) {die('Error: ' . mysql_error());}
		mkdir("user_accounts/$_POST[username]", 0777);
		mkdir("user_accounts/$_POST[username]/experiments", 0777);
		mkdir("user_accounts/$_POST[username]/validate", 0777);
		mkdir("user_accounts/$_POST[username]/countcredit", 0777);
		$msg="Congratulations! You have been successfully registered with MITATE.";
		$start_value = 1000000000;
		$credit_id = $start_value;
		$get_credit_id_counts = mysql_query("SELECT count(*) as count, max(credit_id) as maxval from usercredits");
		while($get_credit_id_count = mysql_fetch_assoc($get_credit_id_counts)) {
			if($get_credit_id_count[count] > 0)
				$credit_id = $get_credit_id_count[maxval] + 1;
		}
		$sql_store_credits ="INSERT INTO usercredits (credit_id, username, available_cellular_credits, contributed_cellular_credits, available_wifi_credits, contributed_wifi_credits) VALUES($credit_id, '$_POST[username]', 200, 0, 500, 0)";
		if (!mysql_query($sql_store_credits, $con)) {die('Error: ' . mysql_error());}			
		mail($_POST[email], "Account Created - MITATE", $msg);
		printf("<script>alert('You have been successfully registered with MITATE.')</script>");
	}
	else {
		printf("<script>alert('The username/email that you have chosen already exists. Please try another one.')</script>");
	} 
	mysql_close($con);
}
?>
