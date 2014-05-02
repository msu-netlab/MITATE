<?php
libxml_use_internal_errors(true); 

$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$passwordEncryptionKey = $xml->database->passwordEncryptionKey;
$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$con)
{
	die('Website down for maintenance. We will be live soon.');
}
mysql_select_db($dbschemaname, $con);
$username = $_POST[username];
$password = $_POST[password];
$encrypted_password = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($passwordEncryptionKey), $password, MCRYPT_MODE_CBC, md5(md5($passwordEncryptionKey))));
$loginresultset = mysql_query("SELECT count(*) as status FROM userinfo where username = '$username' and password = '$encrypted_password' and status = 1");
if ($loginresultset) {
	$loginresultrow = mysql_fetch_assoc($loginresultset);
    if ($loginresultrow['status'] == 1) {
		if($_FILES["file"]["name"] != "") {
			if ($_FILES["file"]["error"] > 0) {
				echo "Error: " . $_FILES["file"]["error"] . "<br />";
			}
			else {
				$start_value = 1000000000;
				$experiment_id = $start_value;
				$get_experiment_id_counts = mysql_query("SELECT count(*) as count, max(experiment_id) as maxval from experiment");
				while($get_experiment_id_count = mysql_fetch_assoc($get_experiment_id_counts)) {
					if($get_experiment_id_count[count] > 0)
						$experiment_id = $get_experiment_id_count[maxval] + 1;
				}
				$file_extension = end(explode(".", $_FILES["file"]["name"]));
				$file_name_without_extension = basename($_FILES["file"]["name"], ".xml");
				$final_file_path = $file_name_without_extension . $experiment_id . "." . $file_extension;
				mkdir("user_accounts/$username/validate/$experiment_id", 0777);
				move_uploaded_file($_FILES["file"]["tmp_name"],"user_accounts/$username/validate/$experiment_id/" . $final_file_path);					
				$filename_validate = "user_accounts/" . $username . "/validate/$experiment_id/" . $final_file_path;
				$xsd_document = 'sample/Mitate_Sample_Configuration_File_XML_Format.xsd';
				$dom = new DomDocument(); 
				if (!$dom->load($filename_validate)) {
					libxml_display_errors();
					Delete("user_accounts/" . $username . "/validate/" . $experiment_id);
				}
				else {
					if (!$dom->schemaValidate($xsd_document)) {	
						libxml_display_errors();  
						Delete("user_accounts/" . $username . "/validate/" . $experiment_id);
					}
					else 
						echo "XML experiment  is valid.";
				}
			}	
		}
		else
			echo "No file specified.";
	}
	else
		echo "Invalid account credentials.";
}

function libxml_display_error($error) { 
	$return = "\n"; 
	switch ($error->level) { 
		case LIBXML_ERR_WARNING: $return .= "Warning $error->code: "; 
								break; 
		case LIBXML_ERR_ERROR: $return .= "Error $error->code: "; 
								break; 
		case LIBXML_ERR_FATAL: $return .= "Fatal Error $error->code: "; 
								break; 
	} 
	$return .= trim($error->message); 
	if ($error->file) { 
		$return .= " in $error->file"; 
	} 
	$return .= " on line $error->line\n"; 
	return $return; 
} 

function libxml_display_errors() { 
	$errors = libxml_get_errors(); 
	foreach ($errors as $error) { 
		print libxml_display_error($error); 
	} 
	libxml_clear_errors(); 
} 

function Delete($path) {
    if (is_dir($path) === true) {
        $files = array_diff(scandir($path), array('.', '..'));
        foreach ($files as $file) {
            Delete(realpath($path) . '/' . $file);
        }
        return rmdir($path);
    }
    else if (is_file($path) === true) {
        return unlink($path);
    }
    return false;
}
?>