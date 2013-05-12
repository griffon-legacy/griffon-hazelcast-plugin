/*
 * Copyright 2011-2013 the original author or authors.
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
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class HazelcastClientHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, HazelcastClient> clients = [:]

    private static final HazelcastClientHolder INSTANCE

    static {
        INSTANCE = new HazelcastClientHolder()
    }

    static HazelcastClientHolder getInstance() {
        INSTANCE
    }

    private HazelcastClientHolder() {}

    String[] getHazelcastClientNames() {
        List<String> clientNames = new ArrayList().addAll(clients.keySet())
        clientNames.toArray(new String[clientNames.size()])
    }

    HazelcastClient getHazelcastClient(String clientName = DEFAULT) {
        if (isBlank(clientName)) clientName = DEFAULT
        retrieveHazelcastClient(clientName)
    }

    void setHazelcastClient(String clientName = DEFAULT, HazelcastClient client) {
        if (isBlank(clientName)) clientName = DEFAULT
        storeHazelcastClient(clientName, client)
    }

    boolean isHazelcastClientConnected(String clientName) {
        if (isBlank(clientName)) clientName = DEFAULT
        retrieveHazelcastClient(clientName) != null
    }
    
    void disconnectHazelcastClient(String clientName) {
        if (isBlank(clientName)) clientName = DEFAULT
        storeHazelcastClient(clientName, null)
    }

    HazelcastClient fetchHazelcastClient(String clientName) {
        if (isBlank(clientName)) clientName = DEFAULT
        HazelcastClient client = retrieveHazelcastClient(clientName)
        if (client == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = HazelcastConnector.instance.createConfig(app)
            client = HazelcastConnector.instance.connect(app, config, clientName)
        }

        if (client == null) {
            throw new IllegalArgumentException("No such hazelcast client configuration for name $clientName")
        }
        client
    }

    private HazelcastClient retrieveHazelcastClient(String clientName) {
        synchronized(LOCK) {
            clients[clientName]
        }
    }

    private void storeHazelcastClient(String clientName, HazelcastClient client) {
        synchronized(LOCK) {
            clients[clientName] = client
        }
    }
}
