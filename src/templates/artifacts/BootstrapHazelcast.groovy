import com.hazelcast.client.HazelcastClient

class BootstrapHazelcast {
    def init = { String clientName, HazelcastClient client ->
    }

    def destroy = { String clientName, HazelcastClient client ->
    }
} 
