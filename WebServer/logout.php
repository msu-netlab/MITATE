<?php
setcookie("username", "", time()-3600);
printf("<script>location.href = 'index.php'</script>");
?>