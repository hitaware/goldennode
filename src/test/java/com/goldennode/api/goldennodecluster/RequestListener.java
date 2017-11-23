package com.goldennode.api.goldennodecluster;

import com.goldennode.api.cluster.ClusterException;
import com.goldennode.api.core.Server;

public interface RequestListener {
    public void _announceServerJoining(Server s) throws ClusterException;

    public void _sendOwnServerIdentity(Server s) ;

    public boolean _acquireProvisionalLeadership(String id) ;

    public boolean _acquireLeadership(String id) ;

    public boolean _releaseProvisionalLeadership(String id) ;

    public boolean _releaseLeadership(String id) ;
}
