import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.javadsl.Source
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by anike_000 on 2/18/2017.
  */
object MyKafkaConsumer {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}

class MyKafkaConsumer(implicit mat: Materializer) extends Actor with ActorLogging {

  import MyKafkaConsumer._

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
      val (control, future) = FileProducerSource.create("MyKafkaConsumer")(context.system)
        .mapAsync(2)(processMessage)
        /*.map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
        .mapAsync(1)(_.commitScaladsl())*/
        .toMat(kafkaSink)(Keep.both)
        .run()
        //.toMat(kafkaSink)(Keep.both)
        //.runWith(kafkaSink)

      //context.stop(self)
  }

  private def processMessage(msg: Message): Future[ProducerRecord[Array[Byte], String]] = {
    println(s"*************************: ${msg.record.value()}")
    var msg2 = new ProducerRecord[Array[Byte], String](TopicDefinition.TOPIC2, msg.record.value().toLowerCase)
    Future.successful(msg2)
  }

  /*private def processMessage2(msg: Message): Future[Message] = {
    val str = s"*************************: ${msg.record.value()}"
    println(s"*************************: ${msg.record.value()}")
    //Future.successful(msg)
    Future.successful(new MyKafkaConsumer[Array[Byte], String](TopicDefinition.TOPIC, str))
  }*/
}
/*      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }

      log.info("Logging consumer started")
  }

  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }

  private def processMessage(msg: Message): Future[Message] = {
    log.info(s"Consumed number: ${msg.record.value()}")
    Future.successful(msg)
  }
}
*/