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

package griffon.plugins.hazelcast;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractHazelcastProvider implements HazelcastProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractHazelcastProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withHazelcast(Closure<R> closure) {
        return withHazelcast(DEFAULT, closure);
    }

    public <R> R withHazelcast(String clientName, Closure<R> closure) {
        if (isBlank(clientName)) clientName = DEFAULT;
        if (closure != null) {
            HazelcastClient client = getHazelcastClient(clientName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on clientName '" + clientName + "'");
            }
            return closure.call(clientName, client);
        }
        return null;
    }

    public <R> R withHazelcast(CallableWithArgs<R> callable) {
        return withHazelcast(DEFAULT, callable);
    }

    public <R> R withHazelcast(String clientName, CallableWithArgs<R> callable) {
        if (isBlank(clientName)) clientName = DEFAULT;
        if (callable != null) {
            HazelcastClient client = getHazelcastClient(clientName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on clientName '" + clientName + "'");
            }
            callable.setArgs(new Object[]{clientName, client});
            return callable.call();
        }
        return null;
    }

    protected abstract HazelcastClient getHazelcastClient(String clientName);
}