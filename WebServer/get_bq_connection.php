<?php
require 'vendor/autoload.php';

use Google\Cloud\ServiceBuilder;

$cloud = new ServiceBuilder();

$bigQuery = null;
$dataset = null;
$bigQuery = $cloud->bigQuery(['projectId' => 'mitate-144222']);
$dataset = $bigQuery->dataset('MITATE');

return [$bigQuery, $dataset];
?>