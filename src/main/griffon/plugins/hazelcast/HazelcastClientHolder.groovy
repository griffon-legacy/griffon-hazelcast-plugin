/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.hazelcast

import com.hazelcast.client.HazelcastClient

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
class HazelcastClientHolder implements HazelcastProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastClientHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, HazelcastClient> clients = [:]

    String[] getClientNames() {
        List<String> clientNames = new ArrayList().addAll(clients.keySet())
        clientNames.toArray(new String[clientNames.size()])
    }

    HazelcastClient getClient(String clientName = 'default') {
        if(isBlank(clientName)) clientName = 'default'
        retrieveClient(clientName)
    }

    void setClient(String clientName = 'default', HazelcastClient client) {
        if(isBlank(clientName)) clientName = 'default'
        storeClient(clientName, client)
    }

    Object withHazelcast(String clientName = 'default', Closure closure) {
        HazelcastClient client = fetchClient(clientName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on client '$clientName'")
        return closure(clientName, client)
    }

    public <T> T withHazelcast(String clientName = 'default', CallableWithArgs<T> callable) {
        HazelcastClient client = fetchClient(clientName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on client '$clientName'")
        callable.args = [clientName, client] as Object[]
        return callable.call()
    }

    boolean isClientConnected(String clientName) {
        if(isBlank(clientName)) clientName = 'default'
        retrieveClient(clientName) != null
    }

    void disconnectClient(String clientName) {
        if(isBlank(clientName)) clientName = 'default'
        storeClient(clientName, null)
    }

    private HazelcastClient fetchClient(String clientName) {
        if(isBlank(clientName)) clientName = 'default'
        HazelcastClient client = retrieveClient(clientName)
        if(client == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = HazelcastConnector.instance.createConfig(app)
            client = HazelcastConnector.instance.connect(app, config, clientName)
        }

        if(client == null) {
            throw new IllegalArgumentException("No such HazelcastClient configuration for name $clientName")
        }
        client
    }

    private HazelcastClient retrieveClient(String clientName) {
        synchronized(LOCK) {
            clients[clientName]
        }
    }

    private void storeClient(String clientName, HazelcastClient client) {
        synchronized(LOCK) {
            clients[clientName] = client
        }
    }
}
