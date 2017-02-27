<?php
require 'vendor/autoload.php';

//use Google\Cloud\BigQuery\BigQueryClient;
//use Google\Cloud\ServiceBuilder;

//$cloud = new ServiceBuilder();

//$bigQuery = $cloud->bigQuery(['projectId' => 'mitate-144222']);

//$bigQuery = new BigQueryClient(['projectId' => 'mitate-144222']);


list($bigQuery, $dataset) = require 'get_bq_connection.php';
//$dataset = $bigQuery->dataset('test');
$table = $dataset->table('test');


$insertResponse = $table->insertRow(['name' => 'Emi', 'id' => 164]);
if (!$insertResponse->isSuccessful()) {
    $row = $insertResponse->failedRows()[0];

    print_r($row['rowData']);

    foreach ($row['errors'] as $error) {
        echo $error['reason'] . ': ' . $error['message'] . PHP_EOL;
    }
}

/*$data = array(array('name' => 'Brian', 'id' => 1245), array('name' => 'Derek', 'id' => 3698));
//$rows = array();
foreach ($data as $row) {
    $insertResponse = $table->insertRow($row);
    if (!$insertResponse->isSuccessful()) {
        $row = $insertResponse->failedRows()[0];

        print_r($row['rowData']);

        foreach ($row['errors'] as $error) {
            echo $error['reason'] . ': ' . $error['message'] . PHP_EOL;
        }
    }
}*/


$queryResults = $bigQuery->runQuery('SELECT name, id FROM [mitate-144222:test.test] ORDER BY name ASC');

$isComplete = $queryResults->isComplete();

while (!$isComplete) {
    sleep(1);
    $queryResults->reload();
    $isComplete = $queryResults->isComplete();
}

echo 'Query complete!';

foreach ($queryResults->rows() as $row) {
    print_r($row);
}
?>