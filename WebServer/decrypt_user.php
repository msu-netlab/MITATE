<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
$encryptionPasswordKey = $xml->userApi->encryptionPasswordKey;

$user_String = $_POST[string];
$decrypted_string = rtrim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, md5($encryptionPasswordKey), base64_decode($user_String), MCRYPT_MODE_CBC, md5(md5($encryptionPasswordKey))), "\0");
echo $decrypted_string;
?>