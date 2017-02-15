<?php
require 'vendor/autoload.php';

use Google\Cloud\BigQuery\BigQueryClient;
use Google\Cloud\ServiceBuilder;

$cloud = new ServiceBuilder();

$bigQuery = $cloud->bigQuery(['projectId' => 'mitate-144222']);

//$bigQuery = new BigQueryClient(['projectId' => 'mitate-144222']);

$dataset = $bigQuery->dataset('test');
$table = $dataset->table('test_metricdata');

$queryResults = $bigQuery->runQuery('SELECT deviceid FROM [mitate-144222:test.test_metricdata] ORDER BY deviceid ASC');

foreach ($queryResults->rows()as $row){
    print_r($row);
}
?>