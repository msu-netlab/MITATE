<?php session_start(); ?>
<html>
<head>
<title>MITATE: Mobile Internet Testbed for ApplicationTraffic Experimentation</title>
</head>
<body style="background-color:white;">
<div style="top:20px;margin-left:auto;margin-right:auto;max-width:1000px;height:auto;">
	<div style="font-size:30px;text-align: center;font-weight: bolder;">MITATE: Mobile Internet Testbed for Application Traffic Experimentation</div>
	<br />
	<hr />
	<div style="background-color:white;height:auto;font-size: 18px;text-align: center;"><a href="index.php" style="text-decoration:none;">Home</a> | <a href="mitate_publications.php" style="text-decoration:none;">Publications</a> | <a href="mitate_signup.php" style="text-decoration:none;">Signup</a> | <a href="https://github.com/msu-netlab/MITATE/" style="text-decoration:none;" target="_blank">Code</a> | <a href="MITATE_Tutorial_For_Beginners.php" style="text-decoration:none;">Tutorial</a> | <a href="mitate_downloads.php" style="text-decoration:none;">Downloads</a> | <a style="text-decoration:none;" href="mailto:mitate@cs.montana.edu">Contact Us</a> | <a style="text-decoration:none;" href="mitate_team.php">Team</a>
	<?php if(isset($_SESSION['mitateLoggedInUser'])) { ?>
	| <a style="color: black;text-decoration:none;" href="mitate_logout.php">Logout</a>
	<?php } ?>
	</div>
	<hr />