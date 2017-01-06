# Synchronizing Tasks across hosts
Hoping to build a simple tool to synchronize tasks across hosts.  Maybe do some basic scheduling as well.


## Ideas
1. Have a master (democratic?, ordered, first come?)
1. Shared work queue
1. Stream queue...transfering larger data across hosts, something kinda zookeeperish...

## How??
Most of this I plan on trying to use Hazelcast for.  Probably really simple for work queue and master, but streams will be a bit more work.