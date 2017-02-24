import FileProducer.{Run, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, Source}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.io.Source._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

//import javax.inject.{ Inject, Singleton }

import akka.actor.{ActorSystem, Props}
import akka.actor._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
//import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
//import org.slf4j.LoggerFactory.


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
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
    }
  }
}
val system = ActorSystem("SimpleSystem")

println("Starting embedded Kafka")
implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(9092, 2181)
EmbeddedKafka.start()
println("Embedded Kafka ready")
implicit val materializer = ActorMaterializer.create(system)

val writer = system.actorOf(Props(new FileProducer))

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