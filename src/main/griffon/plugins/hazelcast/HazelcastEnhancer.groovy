/*
 * Copyright 2012-2013 the original author or authors.
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

import griffon.util.CallableWithArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
final class HazelcastEnhancer {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastEnhancer)

    private HazelcastEnhancer() {}
    
    static void enhance(MetaClass mc, HazelcastProvider provider = DefaultHazelcastProvider.instance) {
        if (LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withHazelcast = {Closure closure ->
            provider.withHazelcast(DEFAULT, closure)
        }
        mc.withHazelcast << {String clientName, Closure closure ->
            provider.withHazelcast(clientName, closure)
        }
        mc.withHazelcast << {CallableWithArgs callable ->
            provider.withHazelcast(DEFAULT, callable)
        }
        mc.withHazelcast << {String clientName, CallableWithArgs callable ->
            provider.withHazelcast(clientName, callable)
        }
    }
}