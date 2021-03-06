/**
 * Copyright 2016 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.awacs.agent;

import io.awacs.core.util.RuntimeHelper;
import io.netty.channel.ChannelFuture;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by antong on 16/9/19.
 */
class NettyChannelPool {

    private GenericObjectPool<ChannelFuture> pool;

    NettyChannelPool(io.netty.bootstrap.Bootstrap bootstrap, List<InetSocketAddress> addresses) {
        PoolableObjectFactory<ChannelFuture> factory = createChannelFactory(bootstrap, addresses);
        this.pool = new GenericObjectPool<>(factory);
        this.pool.setTestOnBorrow(true);
        this.pool.setMaxActive(RuntimeHelper.instance.getProcessors() * 2);
        this.pool.setMaxIdle(RuntimeHelper.instance.getProcessors());
        this.pool.setMinIdle(4);
        this.pool.setMaxWait(3000);
        this.pool.setMinEvictableIdleTimeMillis(15000);
        this.pool.setTimeBetweenEvictionRunsMillis(30000);
    }

    public PoolableObjectFactory<ChannelFuture> createChannelFactory(io.netty.bootstrap.Bootstrap bootstrap,
                                                                     List<InetSocketAddress> addresses) {
        return new NettyChannelFactory(bootstrap, addresses);
    }

    public ChannelFuture borrowChannel() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void returnObject(final ChannelFuture channel) {
        if (channel == null) {
            return;
        }
        try {
            pool.returnObject(channel);
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }

    public void clear() {
        if (pool != null)
            pool.clear();
    }

}
