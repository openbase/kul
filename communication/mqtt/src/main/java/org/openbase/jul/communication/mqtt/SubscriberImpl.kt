package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.Subscriber
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.EventType.Event
import org.openbase.type.communication.ScopeType.Scope
import java.util.*
import java.util.concurrent.Future

class SubscriberImpl(
    scope: Scope, config: CommunicatorConfig
) : RPCCommunicatorImpl(scope, config), Subscriber {

    private var activationFuture: Future<out Any>? = null
    private val isActive: Boolean =
        (activationFuture != null && activationFuture!!.isDone && !activationFuture!!.isCancelled)

    private val callbackMap: MutableMap<UUID, (Event) -> Any> = mutableMapOf()

    override fun registerDataHandler(callback: (Event) -> Any): UUID {
        val handlerId = UUID.randomUUID()
        callbackMap[handlerId] = callback
        return handlerId
    }

    override fun removeDataHandler(handlerId: UUID) {
        callbackMap.remove(handlerId)
    }

    override fun activate() {
        if (isActive) {
            return
        }

        activationFuture = mqttClient.subscribe(
            Mqtt5Subscribe.builder()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .build(),
            { mqtt5Publish: Mqtt5Publish ->
                val event = Event.parseFrom(mqtt5Publish.payloadAsBytes)
                callbackMap.values.forEach { function -> function(event) }
            },
            GlobalCachedExecutorService.getInstance().executorService
        )
    }

    override fun deactivate() {
        activationFuture = null
        mqttClient.unsubscribe(Mqtt5Unsubscribe.builder().topicFilter(topic).build())
    }

    override fun isActive(): Boolean {
        return isActive
    }
}