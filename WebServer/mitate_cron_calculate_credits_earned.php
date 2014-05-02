<?php
$xml = simplexml_load_file("config.xml");
$dbhostname = $xml->databaseConnection->serverAddress;
$dbusername = $xml->databaseConnection->user;
$dbpassword = $xml->databaseConnection->password;
$dbschemaname = $xml->databaseConnection->name;
$con = mysql_connect($dbhostname, $dbusername, $dbpassword);
if (!$con)
{
	die('Website down for maintenance. We will be live soon.');
}
mysql_select_db($dbschemaname, $con);
$get_details = mysql_query("select * from usercredits");
while($get_detail = mysql_fetch_assoc($get_details)) {
	$get_days_left_details = mysql_query("select
	datediff(date_add(date(concat(date_format(current_date, '%Y-%m'),'-',date_format(datecreated, '%d'))), interval 1 month), current_date()) remain from userinfo where username = '$get_detail[username]'");
	$get_days_left_detail = mysql_fetch_assoc($get_days_left_details);
	$days_left_in_month = $get_days_left_detail[remain];
	$x_max_cellular = (float)$get_detail[available_cellular_credits]/$days_left_in_month;
	$x_max_wifi = (float)$get_detail[available_wifi_credits]/$days_left_in_month;
	$x_contributed_cellular = $get_detail[contributed_cellular_credits];
	$x_contributed_wifi = $get_detail[contributed_wifi_credits];
	$get_pings = mysql_query("select usrdvc.pollinterval, usrdvc.timespingedwifi, usrdvc.timespingedcellular
	from userdevice usrdvc, usercredits usrcrt
	where usrdvc.username = usrcrt.username
	and usrcrt.username ='$get_detail[username]'");
	$ping_ratio_cellular = 0;
	$ping_ratio_wifi = 0;
	$number_of_devices = 0;
	while($get_ping = mysql_fetch_assoc($get_pings)) {
		$p_expected = (float)(24 * 60 * 60)/$get_ping[pollinterval];
		$p_actual_cellular = $get_ping[timespingedcellular];
		$p_actual_wifi = $get_ping[timespingedwifi];
		$ping_ratio_cellular = $ping_ratio_cellular + (float)($p_actual_cellular/$p_expected);
		$ping_ratio_wifi = $ping_ratio_wifi + (float)($p_actual_wifi/$p_expected);
		$number_of_devices = $number_of_devices + 1;
	}
	$final_ping_ratio_cellular = (float)$ping_ratio_cellular/$number_of_devices;
	$final_ping_ratio_wifi = (float)$ping_ratio_wifi/$number_of_devices;
	$x_earned_cellular = 0.9 * $x_max_cellular * min(((float)($x_contributed_cellular/$x_max_cellular) + $final_ping_ratio_cellular), 1);
	$x_earned_wifi = 0.9 * $x_max_wifi * min(((float)($x_contributed_wifi/$x_max_wifi) + $final_ping_ratio_wifi), 1);
	mysql_query("update usercredits set available_cellular_credits = (available_cellular_credits + $x_earned_cellular) where username = '$get_detail[username]'", $con);
	mysql_query("update usercredits set available_wifi_credits = (available_wifi_credits + $x_earned_wifi) where username = '$get_detail[username]'", $con);
}
?>