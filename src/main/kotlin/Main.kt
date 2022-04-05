/*
 * Copyright (c) 2022. Francesco Masala <mail@francescomasala.me>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */


import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.io.IOException
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    recieve("localhost", "Test_Queue")
    send("Demo123", "localhost", "Test_Queue")
}

fun send(Message: String, ServerIP: String, QueueName: String) {
    val postman = ConnectionFactory()
    postman.newConnection("amqp://guest:guest@$ServerIP:5672/").use { connection ->
        try {
            connection.createChannel().use {

                    channel ->
                channel.queueDeclare(QueueName, false, false, false, null)
                channel.basicPublish(
                    "",
                    QueueName,
                    null,
                    Message.toByteArray(StandardCharsets.UTF_8)
                )
                println("[Postman] Sent message:\t\t $Message")
            }
        } catch (e: IOException) {
            println(" [!] Failed to connect")
        }
    }
}

fun recieve(ServerIP: String, QueueName: String) {
    val postman = ConnectionFactory()
    val connection = postman.newConnection("amqp://guest:guest@$ServerIP:5672/")
    val channel = connection.createChannel()
    val consumerTag = "Mailbox"

    channel.queueDeclare(QueueName, false, false, false, null)

    println("[$consumerTag] is waiting for messages:")

    val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
        val message = String(delivery.body, StandardCharsets.UTF_8)
        println("[$consumerTag] Received message:\t $message")
    }

    val cancelCallback = CancelCallback { consumerTag: String? -> println("[$consumerTag] was canceled") }

    channel.basicConsume(QueueName, true, consumerTag, deliverCallback, cancelCallback)
}