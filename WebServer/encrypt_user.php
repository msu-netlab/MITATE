<?php

libxml_use_internal_errors(true); 
$xml = simplexml_load_file("config.xml");
$encryptionPasswordKey = $xml->userApi->encryptionPasswordKey;

$user_String = $_POST[string];
$encrypted_string = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5($encryptionPasswordKey), $user_String, MCRYPT_MODE_CBC, md5(md5($encryptionPasswordKey))));
echo $encrypted_string;
?>