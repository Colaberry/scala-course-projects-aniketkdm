import FileProducer.Run
import akka.Done
import akka.actor.{Actor, ActorLogging}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source._

/**
  * Created by anike_000 on 2/18/2017.
  */
object FileProducer{
  case object Run
  case object Stop
}

class FileProducer(implicit mat: Materializer) extends Actor with ActorLogging {

  val config = ConfigFactory.load()

  override def preStart(): Unit = {
    super.preStart()
    self ! Run
  }

  override def receive: Receive = {
    case Run => {
      val filename = config.getString("file.name")

      //val filename = "C:/Colaberry Scala Workspace/scala-course-projects-aniketkdm/Final Project/DataSet/testGenome.csv"

      val lines = fromFile(filename).getLines().drop(1)//.drop(3490)

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")

      log.info("Initializing writer")

      val kafkaSink = Producer.plainSink(producerSettings)

      val done: Future[Done] = Source.fromIterator(() => lines)
        .map(new ProducerRecord[Array[Byte], String](TopicDefinition.TOPIC, _))
        .runWith(kafkaSink)

      done.onComplete({
        success =>
          context.stop(self)
      })


      done.onFailure {
        case ex =>
          println("*********************Stopping********************", ex)
          context.stop(self)
    }
    }
  }
}