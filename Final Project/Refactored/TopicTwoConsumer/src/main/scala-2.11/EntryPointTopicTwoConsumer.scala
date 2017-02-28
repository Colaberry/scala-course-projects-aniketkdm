import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
/**
  * Created by anike_000 on 2/18/2017.
  * This is the starting point of the MicroService
  * That reads the data from topic2 and writes it to elastic search
  */
object EntryPointTopicTwoConsumer extends App{
  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  // here we create a consumer which reads the messages and writes to elastic search
  val topTwoConsumer = system.actorOf(Props(new TopicTwoConsumer()))
}
