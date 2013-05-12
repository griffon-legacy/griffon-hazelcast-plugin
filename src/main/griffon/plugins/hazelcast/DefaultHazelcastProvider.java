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

import com.hazelcast.client.HazelcastClient;

/**
 * @author Andres Almiray
 */
public class DefaultHazelcastProvider extends AbstractHazelcastProvider {
    private static final DefaultHazelcastProvider INSTANCE;

    static {
        INSTANCE = new DefaultHazelcastProvider();
    }

    public static DefaultHazelcastProvider getInstance() {
        return INSTANCE;
    }

    private DefaultHazelcastProvider() {}

    @Override
    protected HazelcastClient getHazelcastClient(String clientName) {
        return HazelcastClientHolder.getInstance().fetchHazelcastClient(clientName);
    }
}
