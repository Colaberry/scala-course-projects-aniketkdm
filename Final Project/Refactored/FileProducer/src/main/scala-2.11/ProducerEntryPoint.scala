import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/**
  * Created by anike_000 on 2/18/2017.
  *
  * This is the main function. This starts the first MicroService that reads the data from a file and writes to a new
  * topic in Kafka stream
  * the producer actor is created and called here.
  *
  */

object ProducerEntryPoint extends App{
  val config = ConfigFactory.load()

  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  val writer = system.actorOf(Props(new FileProducer))
}
