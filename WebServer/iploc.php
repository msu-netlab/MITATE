<?php

$ipaddress = $_SERVER['REMOTE_ADDR']; //ip address
$locationstr="http://api.locatorhq.com/?user=Utkarsh5039&key=fe6d14abca3e24d6e6ed9abdf09858d1ed97351d&ip=$ipaddress&format=xml";

//loading the xml file directly from the website
$xml = simplexml_load_file($locationstr); 

$countrycode = $xml->countryCode; //country code
$countryname = $xml->countryName; //country name
$region = $xml->region; //region name
$city = $xml->city; //city name
$lattitude = $xml->lattitude; //city latitude
$longitude = $xml->longitude; //city longitude
//$browsername = $xml->browserName; //browser name


echo "Location information for <b>".$ipaddress."<br/><br/>";
echo "Country Code: ".$countrycode."<br/>";
echo "Country: ".$countryname."<br/>";
echo "Region: ".$region."<br/>";
echo "City: ".$city."<br/>";
echo "Lattitude: ".$lattitude."<br/>";
echo "Longitude: ".$longitude."<br/>";
?>