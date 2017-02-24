/*import java.util.concurrent.ConcurrentLinkedDeque

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.server.directives._
//import spray.httpx.SprayJsonSupport._
//import com.madhukaraphatak.akkahttp.Models.{Customer, ServiceJsonProtoocol}

/**
  * Created by anike_000 on 2/22/2017.
  */
case class Customer(name: String)

object ServiceJsonProtoocol extends DefaultJsonProtocol {
  implicit val customerProtocol = jsonFormat1(Customer)
}

object AkkaHttpElasticSearch extends App{
  implicit val actorSystem = ActorSystem("rest-api")

  implicit val actorMaterializer = ActorMaterializer()

  val list = new ConcurrentLinkedDeque[Customer]()

  //import ServiceJsonProtoocol.customerProtocol
  val route = {
    import ServiceJsonProtoocol._
    //import spray.http.SprayJsonSupport.sprayJsonUnmarshaller

    path("customer") {
      post {
        entity(as[Customer]) {
          customer =>
            complete {
              list.add(customer)
              s"got customer with name ${customer.name}"
            }
        }
      } ~
        get {
          complete {
            ToResponseMarshallable(list)
          }
        }

    }
  }
  Http().outgoingConnection("192.168.99.100", 9092)
  //Http().bindAndHandle(route, "localhost", 9200)


}
*/