import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}

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

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing my Kafka consumer")
      val (control, future) = FileProducerSource.create("MyKafkaConsumer")(context.system)
        .mapAsync(2)(processMessage)
        .map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
        .run()

      //context.stop(self)
  }

  private def processMessage(msg: Message): Future[Message] = {
    println(s"*************************: ${msg.record.value()}")
    Future.successful(msg)
  }
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