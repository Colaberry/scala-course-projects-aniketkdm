import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.stream.Materializer

import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import net.liftweb.json._
import net.liftweb.json.Serialization.write

import scala.concurrent.Future

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by anike_000 on 2/18/2017.
  */
object TopicTwoConsumer {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}

case class JsonCustomFormat( Sample: String, Family_ID: String,	Population: String,
                        Population_Description: String,  Gender: String)

class TopicTwoConsumer(implicit mat: Materializer) extends Actor with ActorLogging {

  import TopicTwoConsumer._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  var cnt = 0

  override def receive: Receive = {
    case Start =>
      log.info("Initializing Topic Two consumer")
      val (control, future) = TopicTwoConsumerCreator.create("TopicTwoConsumer")(context.system)
          .mapAsync(1)(writeToElasticSearch)
        .mapAsync(1)(processMessage)
          .map(_.committableOffset)
        .toMat(Sink.ignore)(Keep.both)
        .run()
  }

  private def processMessage(msg: Message): Future[Message] = {
    cnt += 1

    log.info(s"Record Number ${cnt} from topic2: ${msg.record.value()}")
    Future.successful(msg)
  }

  def writeToElasticSearch(msg: Message): Future[Message] ={
    implicit val system = ActorSystem("SimpleSystem")

    val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
      Http().outgoingConnection("192.168.99.100", 9200)

    def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
      akka.stream.scaladsl.Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

    def fetchIpInfo(jsonObj: String): Unit = {
      println(jsonObj)
      val request = HttpRequest(POST, uri = "/test/genomeAutoJson/", entity = HttpEntity(ContentTypes.`application/json`,ByteString(jsonObj)))

      println(request)
      ipApiRequest(request).onComplete(
        status =>
          println(status)
      )
    }

    val jsonStr = processToJson(msg.record.value())

    println("calling fetchIpInfo")
    fetchIpInfo(jsonStr)
    println("call done")

    Future.successful(msg)
  }

  def processToJson(strMsg: String): String = {
    val msgArr = strMsg.split(",",6)

    //val msgArr = strMsg.split(/(?>"(?>\\.|[^"])*?"|(,))/, 6)

    val classObj = JsonCustomFormat(msgArr(0),msgArr(1),msgArr(2),msgArr(3),msgArr(4))

    implicit val formats = DefaultFormats

    val jsonString = write(classObj)

    jsonString
  }

}