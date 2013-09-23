<?php
$con = mysql_connect("localhost","mitate","Database4Mitate");
if (!$con)
{
	die('Could not connect: ' . mysql_error());
}
mysql_select_db("mitate", $con);
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("mitate"), $password, MCRYPT_MODE_CBC, md5(md5("mitate"))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$username' and password = '$encrypted_password'");
if ($loginresultset) {
	$loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {	
		echo "Logged in. Please wait...\n";
		$experiment_id = $_POST[experiment_id];
		if($experiment_id != "") {
			$count = 0;
			$get_experiment_list = mysql_query("SELECT * FROM experiment where username = '$username' and experiment_id = $experiment_id");
			while($get_experiment = mysql_fetch_assoc($get_experiment_list)) {
				$count = $count + 1;
			}
			if($count > 0) {
				mysql_query("update experiment set permission = 'public' where experiment_id = $experiment_id", $con);
				echo "The experiment is now public.";
			}
			else
				echo "Permission denied.";
		}
		else
			echo "Missing experiment ID.";
	}
	else
		echo "Invalid account credentials.";
}
?>
