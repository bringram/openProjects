package com.mediocredeveloper.cloud.registry;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.mediocredeveloper.cloud.event.CloudNodeEvent;
import com.mediocredeveloper.cloud.event.CloudNodeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Will2 on 12/3/2016.
 */
class CloudNodeListener implements MembershipListener {

    private static Logger LOGGER = LoggerFactory.getLogger(CloudNodeListener.class);

    private final CloudRegistry registry;
    private final CloudNodeEventListener listener;

    public CloudNodeListener(CloudRegistry registry, CloudNodeEventListener listener){
        this.registry = registry;
        this.listener = listener;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {

    }

    @Override
    public void memberRemoved(MembershipEvent evt) {
        //this local member was lost
        if(evt.getMember().localMember()){
            listener.handle(registry.getGroup(), registry.getName(), CloudNodeEvent.DROPPED);
        }else {
            Set<String> lostNodes = registry.findDownNodes(30, TimeUnit.SECONDS);
            for (String name : lostNodes) {
                listener.handle(registry.getGroup(), name, CloudNodeEvent.DROPPED);
            }
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

    }
}
