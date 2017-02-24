//import javax.inject.{ Inject, Singleton }

//import akka._
import akka.actor.{ActorSystem, Props}
//import akka.actor._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
//import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
//import org.slf4j.LoggerFactory.

/**
  * Created by anike_000 on 2/18/2017.
  */
object EntryPoint extends App{
  val system = ActorSystem("SimpleSystem")

  //println("Starting embedded Kafka")
  //implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(9092, 2181)
  //EmbeddedKafka.start()
  //println("Embedded Kafka ready")
  implicit val materializer = ActorMaterializer.create(system)

  val writer = system.actorOf(Props(new FileProducer))

  Thread.sleep(2000) // for production systems topic auto creation should be disabled

  val consumer = system.actorOf(Props(new MyKafkaConsumer))

  //akka.Main.main(Array(writer))
  
  /*println("Shutting down application...")
  writer ! FileProducer.Stop
  consumer ! MyKafkaConsumer.Stop*/
  //Future.successful(EmbeddedKafka.stop())
}
