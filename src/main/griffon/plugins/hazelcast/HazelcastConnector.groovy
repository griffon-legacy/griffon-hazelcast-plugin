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

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.hazelcast.client.*

/**
 * @author Andres Almiray
 */
@Singleton
final class HazelcastConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConnector)
    private bootstrap

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.hazelcast) {
            app.config.pluginConfig.hazelcast = ConfigUtils.loadConfigWithI18n('HazelcastConfig')
        }
        app.config.pluginConfig.hazelcast
    }

    private ConfigObject narrowConfig(ConfigObject config, String clientName) {
        if (config.containsKey('client') && clientName == DEFAULT) {
            return config.client
        } else if (config.containsKey('clients')) {
            return config.clients[clientName]
        }
        return config
    }

    HazelcastClient connect(GriffonApplication app, ConfigObject config, String clientName = DEFAULT) {
        if (HazelcastClientHolder.instance.isHazelcastClientConnected(clientName)) {
            return HazelcastClientHolder.instance.getHazelcastClient(clientName)
        }

        config = narrowConfig(config, clientName)
        app.event('HazelcastConnectStart', [config, clientName])
        HazelcastClient client = startHazelcast(config)
        HazelcastClientHolder.instance.setHazelcastClient(clientName, client)
        bootstrap = app.class.classLoader.loadClass('BootstrapHazelcast').newInstance()
        bootstrap.metaClass.app = app
        resolveHazelcastProvider(app).withHazelcast { dn, c -> bootstrap.init(dn, c) }
        app.event('HazelcastConnectEnd', [clientName, client])
        client
    }

    void disconnect(GriffonApplication app, ConfigObject config, String clientName = DEFAULT) {
        if (HazelcastClientHolder.instance.isHazelcastClientConnected(clientName)) {
            config = narrowConfig(config, clientName)
            HazelcastClient client = HazelcastClientHolder.instance.getHazelcastClient(clientName)
            app.event('HazelcastDisconnectStart', [config, clientName, client])
            resolveHazelcastProvider(app).withHazelcast { dn, c -> bootstrap.destroy(dn, c) }
            stopHazelcast(config, client)
            app.event('HazelcastDisconnectEnd', [config, clientName])
            HazelcastClientHolder.instance.disconnectHazelcastClient(clientName)
        }
    }

    HazelcastProvider resolveHazelcastProvider(GriffonApplication app) {
        def hazelcastProvider = app.config.hazelcastProvider
        if (hazelcastProvider instanceof Class) {
            hazelcastProvider = hazelcastProvider.newInstance()
            app.config.hazelcastProvider = hazelcastProvider
        } else if (!hazelcastProvider) {
            hazelcastProvider = DefaultHazelcastProvider.instance
            app.config.hazelcastProvider = hazelcastProvider
        }
        hazelcastProvider
    }

    private HazelcastClient startHazelcast(ConfigObject config) {
        ClientConfig clientConfig = new ClientConfig()
        config.each { key, value ->
            try {
                clientConfig[key] = value
            } catch(MissingPropertyException e) {
                // ignore
            }
        }
        HazelcastClient.newHazelcastClient(clientConfig)
    }

    private void stopHazelcast(ConfigObject config, HazelcastClient client) {
        client.shutdown()
    }
}