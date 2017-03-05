<?php
libxml_use_internal_errors(true);
$xml = simplexml_load_file("config.xml");
list($bigQuery, $dataset) = require 'get_bq_connection.php';
if (!$bigQuery) {
    die('Could not connect to BigQuery');
}
$k = 0;
$time = str_replace("T", " ", $_POST[time]);
if ($_POST[oneway] == 0 && $_POST[size] == 0 && $_POST[rtt] == 0) {
    $logsTable = $dataset->table('logs');
    $insertResponse = $logsTable->insertRow(['username' => $_POST[username], 'transferid' => $_POST[transferid], 'deviceid' => $_POST[deviceid], 'logmessage' => $_POST[log], 'transferfinished' => $_POST[time]]);
    if (!$insertResponse->isSuccessful()){
        die(printErrors($insertResponse));
    }
} else {
    $metricDataTable = $dataset->table('metricdata');
    $insertResponse = $metricDataTable->insertRow(['metricid' => 10027, 'transferid' => $_POST[transferid], 'transactionid' => $_POST[transactionid], 'value' => $_POST[oneway], 'transferfinished' => $time, 'deviceid' =>$_POST[deviceid]]);
    if (!$insertResponse->isSuccessful()){
        die(printErrors($insertResponse));
    } else $k = $k + 1;

    $insertResponse = $metricDataTable->insertRow(['metricid' => 10028, 'transferid' => $_POST[transferid], 'transactionid' => $_POST[transactionid], 'value' => $_POST[rtt], 'transferfinished' => $time, 'deviceid' =>$_POST[deviceid]]);
    if (!$insertResponse->isSuccessful()){
        die(printErrors($insertResponse));
    } else $k = $k + 1;

    $onewaythrouhput = $_POST[size] / $_POST[oneway];
    $insertResponse = $metricDataTable->insertRow(['metricid' => 10029, 'transferid' => $_POST[transferid], 'transactionid' => $_POST[transactionid], 'value' => $onewaythrouhput, 'transferfinished' => $time, 'deviceid' =>$_POST[deviceid]]);
    if (!$insertResponse->isSuccessful()){
        die(printErrors($insertResponse));
    } else $k = $k + 1;


    $sql = "update metricdata set transferfinished = '$time' where transferid = $_POST[transferid] and transactionid = $_POST[transactionid] and deviceid = '$_POST[deviceid]'";
    if (!mysql_query($sql, $bigQuery)) {
        die('Error: ' . mysql_error());
    } else $k = $k + 1;

    $transferExecutedByTable = $dataset->table('transferexecutedby');
    $insertResponse = $transferExecutedByTable->insertRow(['transferid' => $_POST[transferid], 'devicename' => $_POST[devicename], 'username' => $_POST[username], 'carriername' => $_POST[mobilecarrier], 'deviceid' => $_POST[deviceid]]);
    if (!$insertResponse->isSuccessful()){
        die(printErrors($insertResponse));
    } else $k = $k + 1;

    if ($k == 5) {
        $insertResponse = $metricDataTable->insertRow(['metricid' => 10040, 'transferid' => $_POST[transferid], 'transactionid' => $_POST[transactionid], 'transferfinished' => $time, 'deviceid' =>$_POST[deviceid], 'responsedata' => $_POST[log]]);
        if (!$insertResponse->isSuccessful()){
            die(printErrors($insertResponse));
        }
        echo "1";
    } else
        echo "0";
}

function printErrors($response)
{
    $row = $response->failedRows()[0];

    print_r($row['rowData']);

    foreach ($row['errors'] as $error) {
        echo $error['reason'] . ': ' . $error['message'] . PHP_EOL;
    }
}

?>