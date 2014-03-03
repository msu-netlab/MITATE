select DISTINCT md.deviceid as deviceId, ud.devicename as deviceName, ud.devicecarrier as deviceCarrier 
from experiment exp, transactions trans, trans_transfer_link ttl, metricdata md, userdevice ud 
where exp.experiment_id = 1000000005 -- replace the experiment ID with the experiment ID you got in step 16
and trans.experiment_id = exp.experiment_id 
and ttl.transactionid = trans.transactionid 
and ttl.transferid = md.transferid 
and md.deviceid = ud.deviceid 