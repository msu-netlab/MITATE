select mt.name as metric_name, md.value as tcp_uplink_latency 
from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md 
where exp.experiment_id = 1000000005 -- replace the experiment ID with the experiment ID you got in step 16
and trans.experiment_id = exp.experiment_id
and ttl.transactionid = trans.transactionid 
and ttl.transferid = md.transferid 
and md.metricid = mt.metricid 
and mt.name in ('tcp_min_uplink_delay', 'tcp_mean_uplink_delay', 'tcp_max_uplink_delay') 