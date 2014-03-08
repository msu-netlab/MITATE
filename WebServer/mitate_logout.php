<?php
session_start();
if(isset($_SESSION['mitateLoggedInUser']))
  unset($_SESSION['mitateLoggedInUser']);
  printf("<script>location.href = 'index.php'</script>");
?>