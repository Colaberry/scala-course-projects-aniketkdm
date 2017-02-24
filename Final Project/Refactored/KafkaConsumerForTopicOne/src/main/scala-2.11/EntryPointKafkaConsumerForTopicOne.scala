import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer

/**
  * Created by anike_000 on 2/18/2017.
  */
object EntryPointKafkaConsumerForTopicOne extends App {
  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  val consumer = system.actorOf(Props(new MyKafkaConsumerForTopicOne()))
}
