import FileProducer.{Run, Stop}
import akka.actor.{Actor, ActorLogging, Cancellable}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.io.Source._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

/**
  * Created by anike_000 on 2/18/2017.
  */
object FileProducer{
  case object Run
  case object Stop
}

class FileProducer(implicit mat: Materializer) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    super.preStart()
    self ! Run
  }

  override def receive: Receive = {
    case Run => {
      val filename = "C:/Colaberry Scala Workspace/scala-course-projects-aniketkdm/Final Project/DataSet/1000-genomes%2Fother%2Fsample_info%2Fsample_info.csv"

      val lines = fromFile(filename).getLines().drop(3490)

      val ticks = Source.tick(Duration.Zero, 1.seconds, Unit).map(_ => lines.next())

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")

      log.info("Initializing writer")

      val kafkaSink = Producer.plainSink(producerSettings)

      val (control, future) = ticks
        .map(new ProducerRecord[Array[Byte], String](TopicDefinition.TOPIC, _))
        .toMat(kafkaSink)(Keep.both)
        .run()

      future.onFailure {
        case ex =>
          println("*********************Stopping********************")
          context.stop(self)
      }
    }
  }
}
      /*context.become(running(control))
      log.info(s"Writer now running, writing random numbers to topic ${TopicDefinition.TOPIC}")


    }
  }

  def running(control: Cancellable): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      control.cancel()
      context.stop(self)
  }
}
*/