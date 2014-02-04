<br /><br />
	<hr />
	<div style="background-color:grey;height:auto;font-size: 18px;color: white;text-align: center;"><a href="index.php" style="color: white;text-decoration:none;">Home</a> | <a href="mitate_publications.php" style="color: white;text-decoration:none;">Publications</a> | <a href="mitate_signup.php" style="color: white;text-decoration:none;">Signup</a> | <a href="https://github.com/msu-netlab/MITATE/" style="color: white;text-decoration:none;" target="_blank">Code</a> | <a href="mitate_downloads.php" style="color: white;text-decoration:none;">Downloads</a> | <a style="color: white;text-decoration:none;" href="mailto:mitate@cs.montana.edu">Contact Us</a> | <a style="color: white;text-decoration:none;" href="mitate_team.php">Team</a>
	<?php if(isset($_SESSION['mitateLoggedInUser'])) { ?>
	| <a style="color: white;text-decoration:none;" href="mitate_logout.php">Logout</a>
	<?php } ?>
	</div>
</div>
<br />
</body>
</html>