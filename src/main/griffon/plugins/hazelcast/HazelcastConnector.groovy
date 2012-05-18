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

import com.hazelcast.client.*

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.CallableWithArgs

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class HazelcastConnector implements HazelcastProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConnector)

    Object withHazelcast(String clientName = 'default', Closure closure) {
        HazelcastClientHolder.instance.withHazelcast(clientName, closure)
    }

    public <T> T withHazelcast(String clientName = 'default', CallableWithArgs<T> callable) {
        return HazelcastClientHolder.instance.withHazelcast(clientName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        def clientClass = app.class.classLoader.loadClass('HazelcastConfig')
        new ConfigSlurper(Environment.current.name).parse(clientClass)
    }

    private ConfigObject narrowConfig(ConfigObject config, String clientName) {
        return clientName == 'default' ? config.client : config.clients[clientName]
    }

    HazelcastClient connect(GriffonApplication app, ConfigObject config, String clientName = 'default') {
        if (HazelcastClientHolder.instance.isClientConnected(clientName)) {
            return HazelcastClientHolder.instance.getClient(clientName)
        }

        config = narrowConfig(config, clientName)
        app.event('HazelcastConnectStart', [config, clientName])
        HazelcastClient client = startHazelcast(config)
        HazelcastClientHolder.instance.setClient(clientName, client)
        bootstrap = app.class.classLoader.loadClass('BootstrapHazelcast').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(clientName, client)
        app.event('HazelcastConnectEnd', [clientName, client])
        client
    }

    void disconnect(GriffonApplication app, ConfigObject config, String clientName = 'default') {
        if (HazelcastClientHolder.instance.isClientConnected(clientName)) {
            config = narrowConfig(config, clientName)
            HazelcastClient client = HazelcastClientHolder.instance.getClient(clientName)
            app.event('HazelcastDisconnectStart', [config, clientName, client])
            bootstrap.destroy(clientName, client)
            stopHazelcast(config, client)
            app.event('HazelcastDisconnectEnd', [config, clientName])
            HazelcastClientHolder.instance.disconnectClient(clientName)
        }
    }

    private HazelcastClient startHazelcast(ConfigObject config) {
        ClientConfig clientConfig = new ClientConfig()
        config.each { key, value -> clientConfig[key] = value }
        HazelcastClient.newHazelcastClient(clientConfig)
    }

    private void stopHazelcast(ConfigObject config, HazelcastClient client) {
        client.shutdown()
    }
}
