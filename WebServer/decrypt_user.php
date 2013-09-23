<?php
$user_String = $_POST[string];
$decrypted_string = rtrim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, md5("nwlabcsmontana.edu"), base64_decode($user_String), MCRYPT_MODE_CBC, md5(md5("nwlabcsmontana.edu"))), "\0");
echo $decrypted_string;
?>