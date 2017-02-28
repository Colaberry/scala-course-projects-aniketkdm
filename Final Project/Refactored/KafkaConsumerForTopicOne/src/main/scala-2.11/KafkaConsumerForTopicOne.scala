import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.ProducerSettings

import akka.kafka.scaladsl.Producer
import akka.stream.Materializer

import akka.stream.scaladsl.{Keep, Sink}

import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

import scala.language.postfixOps


/**
  * Created by anike_000 on 2/18/2017.
  * This consumer consumes the messages from Topic One.
  * Processes the received messages (for now just converting to lowercase)
  * And writes the processed messages to Topic2
  */


/**
  * The Object creates two case object messages
  */
object MyKafkaConsumerForTopicOne {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}

class MyKafkaConsumerForTopicOne(implicit mat: Materializer) extends Actor with ActorLogging {

  import MyKafkaConsumerForTopicOne._

  val producerSettings2 = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val kafkaSink = Producer.plainSink(producerSettings2)

  /**
    * The preStart method sends the Start type message to itself
    * To start the processing of messages
    * And to load it to Topic2
    */

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing my Kafka consumer")
      val (control, future) = SourceKafkaConsumerForTopicOne.create("MyKafkaConsumer")(context.system)
        .mapAsync(2)(processMessage)
        .toMat(kafkaSink)(Keep.both)
        .run()
  }

  /**
    * processMessage Method takes in the received message
    * performs the transformation (for now just converting to lowerCase)
    * And returns ProducerRecord to be written in Topic2
    */
  private def processMessage(msg: Message): Future[ProducerRecord[Array[Byte], String]] = {
    println(s"****MyKafkaConsumer*****: ${msg.record.value()}")
    var msg2 = new ProducerRecord[Array[Byte], String](TopicDefinition.TOPIC2, msg.record.value().toLowerCase)
    Future.successful(msg2)
  }

}
