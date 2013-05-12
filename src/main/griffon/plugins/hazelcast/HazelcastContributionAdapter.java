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

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class HazelcastContributionAdapter implements HazelcastContributionHandler {
    private static final String DEFAULT = "default";

    private HazelcastProvider provider = DefaultHazelcastProvider.getInstance();

    public void setHazelcastProvider(HazelcastProvider provider) {
        this.provider = provider != null ? provider : DefaultHazelcastProvider.getInstance();
    }

    public HazelcastProvider getHazelcastProvider() {
        return provider;
    }

    public <R> R withHazelcast(Closure<R> closure) {
        return withHazelcast(DEFAULT, closure);
    }

    public <R> R withHazelcast(String clientName, Closure<R> closure) {
        return provider.withHazelcast(clientName, closure);
    }

    public <R> R withHazelcast(CallableWithArgs<R> callable) {
        return withHazelcast(DEFAULT, callable);
    }

    public <R> R withHazelcast(String clientName, CallableWithArgs<R> callable) {
        return provider.withHazelcast(clientName, callable);
    }
}