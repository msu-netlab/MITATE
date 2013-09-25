<?php
libxml_use_internal_errors(true); 

$con = mysql_connect("nl.cs.montana.edu","mitate","Database4Mitate");
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
		$filename = 'sample/Mitate_Sample_Configuration_File_XML_Format.xml';
		$xsd_document = 'sample/Mitate_Sample_Configuration_File_XML_Format.xsd';
		$dom = new DomDocument(); 
		if (!$dom->load($filename)) 
			echo "We detected errors in your XML file. It is recommended that you must open your XML in browser to check for any syntactical errors.";
		else 
			if (!$dom->schemaValidate($xsd_document)) 			
				libxml_display_errors();  
			else 
				echo "Your XML is validated.";
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
?>