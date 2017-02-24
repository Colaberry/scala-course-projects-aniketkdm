import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
/**
  * Created by anike_000 on 2/18/2017.
  */
object EntryPointTopicTwoConsumer extends App{
  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  val topTwoConsumer = system.actorOf(Props(new TopicTwoConsumer()))
}
