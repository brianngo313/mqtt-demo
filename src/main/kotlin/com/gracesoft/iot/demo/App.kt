package com.gracesoft.iot.demo

import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

fun main(args: Array<String>) {
    println(App().greeting)
    createSubscriber()
    val publisher = createPublisher()
    while (true) {
        publish(publisher)
        Thread.sleep(2000)
    }
}

private fun createPublisher(): IMqttClient {
    val publisherId = UUID.randomUUID().toString()
    val publisher: IMqttClient = MqttClient("tcp://localhost:1883", publisherId)

    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true
    options.isCleanSession = true
    options.connectionTimeout = 10
    publisher.connect(options)

    return publisher
}

private fun publish(publisher: IMqttClient) {
    if (!publisher.isConnected) {
        return
    }

    val temp = 80 + (0..20).random()
    val payload = "T:$temp".toByteArray()
    val msg = MqttMessage(payload)
    msg.qos = 2
    msg.isRetained = true

    publisher.publish("/dummy/temp/1", msg)
}

fun createSubscriber() {
    val subscriberId = UUID.randomUUID().toString()
    val subscriber: IMqttClient = MqttClient("tcp://localhost:1883", subscriberId)

    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true
    options.isCleanSession = true
    options.connectionTimeout = 10
    subscriber.connect(options)

    while (!subscriber.isConnected) {
        println("Waiting for connection")
        Thread.sleep(1000)
    }

    subscriber.subscribe("/dummy/temp/1") { topic, msg ->
        val payload = String(msg.payload)
        println("Received message $payload from topic: $topic")
    }
}
