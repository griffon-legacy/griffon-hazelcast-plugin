/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
 class HazelcastGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-hazelcast-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Hazelcast support'
    String description = '''
The Hazelcast plugin enables lightweight access to [Hazelcast][1] datastores.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * HazelcastConfig.groovy - contains the database definitions.
 * BootstrapHazelcast.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withHazelcast` will be injected into all controllers,
giving you access to a `com.hazelcast.client.HazelcastClient` object, with which you'll be able
to make calls to the database. Remember to make all database calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.
This method is aware of multiple databases. If no clientName is specified when calling
it then the default database will be selected. Here are two example usages, the first
queries against the default database while the second queries a database whose name has
been configured as 'internal'

	package sample
	class SampleController {
	    def queryAllDatabases = {
	        withHazelcast { clientName, client -> ... }
	        withHazelcast('internal') { clientName, client -> ... }
	    }
	}
	
This method is also accessible to any component through the singleton `griffon.plugins.hazelcast.HazelcastConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`HazelcastEnhancer.enhance(metaClassInstance, hazelcastProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withHazelcast()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.hazelcast.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * HazelcastConnectStart[config, clientName] - triggered before connecting to the database
 * HazelcastConnectEnd[clientName, client] - triggered after connecting to the database
 * HazelcastDisconnectStart[config, clientName, client] - triggered before disconnecting from the database
 * HazelcastDisconnectEnd[config, clientName] - triggered after disconnecting from the database

### Multiple Stores

The config file `HazelcastConfig.groovy` defines a default client block. As the name
implies this is the client used by default, however you can configure named clients
by adding a new config block. For example connecting to a client whose name is 'internal'
can be done in this way

	clients {
        internal {
            addresses = ['127.0.0.1:5701']
        }
	}

This block can be used inside the `environments()` block in the same way as the
default client block is used.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/hazelcast][2]

Testing
-------
The `withHazelcast()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `HazelcastEnhancer.enhance(metaClassInstance, hazelcastProviderInstance)` where 
`hazelcastProviderInstance` is of type `griffon.plugins.hazelcast.HazelcastProvider`. The contract for this interface looks like this

    public interface HazelcastProvider {
        Object withHazelcast(Closure closure);
        Object withHazelcast(String clientName, Closure closure);
        <T> T withHazelcast(CallableWithArgs<T> callable);
        <T> T withHazelcast(String clientName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyHazelcastProvider implements HazelcastProvider {
        Object withHazelcast(String clientName = 'default', Closure closure) { null }
        public <T> T withHazelcast(String clientName = 'default', CallableWithArgs<T> callable) { null }      
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            HazelcastEnhancer.enhance(service.metaClass, new MyHazelcastProvider())
            // exercise service methods
        }
    }


[1]: http://hazelcast.com
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/hazelcast
'''
}
