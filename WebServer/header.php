<?php session_start(); ?>
<html>
<head>
    <link rel="shortcut icon" type="image/x-icon" href="https://www.cs.montana.edu/sites/default/files/favicon.ico"/>
    <title>MITATE: Mobile Internet Testbed for Application Traffic Experimentation</title>
</head>
<body style="background-color:white;">
<div style="top:20px;margin-left:auto;margin-right:auto;max-width:1000px;height:auto;">
    <div style="font-size:30px;text-align: center;font-weight: bolder;">MITATE: Mobile Internet Testbed for Application
        Traffic Experimentation
    </div>
    <br/>
    <hr/>
    <div style="background-color:white;height:auto;font-size: 18px;text-align: center;"><a href="index.php"
                                                                                           style="text-decoration:none;">Home</a>
        | <a href="https://github.com/msu-netlab/MITATE/" style="text-decoration:none;" target="_blank">Source Code</a>
        | <a href="mitate_tutorial_for_beginners.php" style="text-decoration:none;">Tutorial</a>


        | <a href="mitate_downloads.php"
             style="text-decoration:none;">Downloads</a>
        <?php if (!isset($_SESSION['mitateLoggedInUser'])) { ?> | <a style="text-decoration:none;"
                                                                     href="mitate_signin.php">Sign In</a> | <a
                href="mitate_signup.php"
                style="text-decoration:none;">Register</a>    <?php } ?>

        <?php if (isset($_SESSION['mitateLoggedInUser'])) { ?>
            | <a href="mitate_publications.php" style="text-decoration:none;">Publications</a> |
            <a style="text-decoration:none;" href="mitate_team.php">People</a> |
            <a style="text-decoration:none;" href="settings.php">Settings</a> |
            <a style="text-decoration:none;" href="mitate_logout.php">Logout</a>
        <?php } ?>
    </div>
    <hr/>