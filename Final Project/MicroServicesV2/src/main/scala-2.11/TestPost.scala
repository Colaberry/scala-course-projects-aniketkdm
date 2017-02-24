import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer

object testMain extends App{

  val system = ActorSystem("SimpleSystem")

  implicit val materializer = ActorMaterializer.create(system)

  /*val testTrainingStr = "{" +
    "\"firstName\": \"Logan\"" +
    "\"lastName\": \"Ye\"" +
    "\"email\": \"Logan@colaberry\"" +
    "\"interests\": \"[\"Coding\",\"Machine Learning\"]" + "}"*/

  val consumer = system.actorOf(Props(new TestPostConsumer))
  //TestPost.fetchIpInfo(testTrainingStr)

}