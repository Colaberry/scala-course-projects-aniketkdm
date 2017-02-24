import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/**
  * Created by anike_000 on 2/18/2017.
  */

object ProducerEntryPoint extends App{
  val config = ConfigFactory.load()

  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  val writer = system.actorOf(Props(new FileProducer))
}
