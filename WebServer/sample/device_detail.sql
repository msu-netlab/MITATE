select DISTINCT md.deviceid as deviceId, ud.devicename as deviceName, ud.devicecarrier as deviceCarrier 
from experiment exp, transactions trans, trans_transfer_link ttl, metricdata md, userdevice ud 
where exp.experiment_id = 1000000005 -- you may want to change this
and trans.experiment_id = exp.experiment_id 
and ttl.transactionid = trans.transactionid 
and ttl.transferid = md.transferid 
and md.deviceid = ud.deviceid 