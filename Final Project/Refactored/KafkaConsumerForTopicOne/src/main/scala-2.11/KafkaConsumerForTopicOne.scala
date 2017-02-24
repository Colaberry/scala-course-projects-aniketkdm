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

  private def processMessage(msg: Message): Future[ProducerRecord[Array[Byte], String]] = {
    println(s"****MyKafkaConsumer*****: ${msg.record.value()}")
    var msg2 = new ProducerRecord[Array[Byte], String](TopicDefinition.TOPIC2, msg.record.value().toLowerCase)
    Future.successful(msg2)
  }

}
