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

/**
  * ApiMain opens a port for the user
  * The port number is mentioned in the application.conf
  * This is to demonstrate a mechanism where we provide a user with an interface
  * Receive a request on the opened custom port
  * Process the request (here we just fetch required information from elastic search)
  * And provide the received feedback to the user
  */

import scala.concurrent.Future

trait worker{

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def config: Config
  val logger: LoggingAdapter

  // sets up Http outgoing connection to connect to elastic search as per conf file
  val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("elasticSearch.url"), config.getInt("elasticSearch.port"))

  // via flow setup to get the data from elastic search
  def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
    akka.stream.scaladsl.Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

  /**
    * this method fetches the information from the elastic search
    */
  def fetchStudentInfo(id: String): Future[Either[String, String]] = {
    //ipApiRequest(RequestBuilding.Get(s"/genomestage/id/$id")).flatMap { response =>
    ipApiRequest(RequestBuilding.Get(config.getString("elasticSearch.path")+id)).flatMap { response =>
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

  /**
    * request is received from "http://localhost:8080/id/{id}
    * (because localhost and 8080 are mentioned in the conf file) This can be changed as per requirement
    * The id number passed is given to fetchStudentInfo method
    * which reads the data from elastic search configuration mentioned in application.conf
    * and returns either corresponding data or the Badrequest error
    */

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

/**
  * Starting point
  */
object ApiMain extends App with worker {

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  // binds the routes method with the api url and api port mentioned in the application.conf
  Http().bindAndHandle(routes, config.getString("api.url"), config.getInt("api.port"))

}