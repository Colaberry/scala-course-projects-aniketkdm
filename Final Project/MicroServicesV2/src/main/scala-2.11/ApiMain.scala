import java.io.IOException

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives.{complete, get, logRequestResult, path, pathPrefix}
import akka.http.scaladsl.server.PathMatchers.{IntNumber, Segment}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol

import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.Serialization.write

import scala.concurrent.Future

trait Protocols extends DefaultJsonProtocol {
  implicit val studentInfoFormat = jsonFormat5(JsonCustomFormat.apply)
}

trait worker extends Protocols{

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  //implicit val marsh(_) = ToResponseMarshallable[JsonCustomFormat](JsonCustomFormat)

  def config: Config
  val logger: LoggingAdapter

  val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection("192.168.99.100", 9200)

  def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
    akka.stream.scaladsl.Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

  def fetchStudentInfo(id: String): Future[Either[String, String]] = {
    ipApiRequest(RequestBuilding.Get(s"/test/genomeAutoJson/$id")).flatMap { response =>
      response.status match {
        case OK => Future.successful(Right(response.entity.toString))
        case BadRequest => Future.successful(Left(s"$id: Incorrect ID Number"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Fetching student info failed with status: ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  val routes = {
    logRequestResult("Http-Api-MicroService") {
      pathPrefix("id") {
        (get & path(Segment)) { id =>
          complete {
            fetchStudentInfo(id).map[ToResponseMarshallable] {
              case Right(jsonCustomFormat) => jsonCustomFormat
              //case Right(jsonCustomFormat) => jsonCustomFormat
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
  }
}
object ApiMain extends App with worker {
/*  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()*/

  //val ApiConsumer = system.actorOf(Props(new ApiGetConsumer))
  //TestPost.fetchIpInfo(testTrainingStr)

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "localhost", 8080)

}