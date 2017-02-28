import akka.actor.{Actor, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.model.HttpMethods._
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global

//val logger: LoggingAdapter

/**
  * Created by anike_000 on 2/22/2017.
  */
object TestPostConsumer{
  case object Start
  case object Stop
}

class TestPostConsumer(implicit mat: Materializer) extends Actor {

  implicit val system = ActorSystem("SimpleSystem")

  import TestPostConsumer._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  //case class training(firstName: String, lastName: String, email: String, interests: Array[String])

  override def receive: Receive = {
    case Start => {
      println("started")
      val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
        Http().outgoingConnection("192.168.99.100", 9200)

      def ipApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

      val testTrainingStr = "{" +
        "\"firstName\": \"Adela\"" + "," +
        "\"lastName\": \"Wee\"," +
        "\"email\": \"Ade@colaberry\"," +
        "\"interests\": [\"Robotics\",\"Coding\",\"Machine Learning\"]" + "}"



      def fetchIpInfo(jsonObj: String): Unit = {
        println(jsonObj)
        val request = HttpRequest(POST, uri = "/newuri/student/", entity = HttpEntity(ContentTypes.`application/json`,ByteString(jsonObj)))
        //val request = HttpRequest(POST, uri = "/training/student/11", entity = HttpEntity(ContentTypes.`application/json`,jsonObj))
        println(request)
        ipApiRequest(request).onComplete(
          status =>
            println(status)
        )
      }

      println("calling fetchIpInfo")
      fetchIpInfo(testTrainingStr)
      println("call done")
    }
  }
}
  //val testTrainingObj = training("Logan","Wang","logan@colaberry",Array("Coding","Machine Learning"))

