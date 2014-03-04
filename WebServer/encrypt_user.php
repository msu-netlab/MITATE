<?php
$user_String = $_POST[string];
$encrypted_string = base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, md5("nwlabcsmontana.edu"), $user_String, MCRYPT_MODE_CBC, md5(md5("nwlabcsmontana.edu"))));
echo $encrypted_string;
?>