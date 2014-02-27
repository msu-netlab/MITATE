select md.value as uplink_tcp_max_jitter 
from experiment exp, transactions trans, trans_transfer_link ttl, metric mt, metricdata md 
where exp.experiment_id = 1000000005 -- you may want to change this
and trans.experiment_id = exp.experiment_id 
and ttl.transactionid = trans.transactionid 
and ttl.transferid = md.transferid 
and md.metricid = mt.metricid 
and mt.name = 'tcp_uplink_packet_loss' 