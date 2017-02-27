<?php
require 'vendor/autoload.php';

use Google\Cloud\ServiceBuilder;

$cloud = new ServiceBuilder();

$bigQuery = $cloud->bigQuery(['projectId' => 'mitate-144222']);
$dataset = $bigQuery->dataset('test');

return [$bigQuery, $dataset];
?>