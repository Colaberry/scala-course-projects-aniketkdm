import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import net.liftweb.json.Serialization.write

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by anike_000 on 2/18/2017.
  * Consumer to perform MicroService 3 operations
  */

/**
  * This Object creates two message objects
  */
object TopicTwoConsumer {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}

/**
  * This is the class that is used to convert the messages read from topic two to Json String
  * Data is written to elastic search in Json format
  * @param Sample
  * @param Family_ID
  * @param Population
  * @param Population_Description
  * @param Gender
  */
case class JsonCustomFormat( Sample: String, Family_ID: String,	Population: String,
                        Population_Description: String,  Gender: String)

class TopicTwoConsumer(implicit mat: Materializer) extends Actor with ActorLogging {

  import TopicTwoConsumer._
  val config = ConfigFactory.load()

  /**
    * start message is send to self in preStart
    * The start message processing is where the data is written to elastic search
    */
  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  var cnt = 0

  override def receive: Receive = {
    case Start =>
      log.info("Initializing Topic Two consumer")
      val (control, future) = TopicTwoConsumerCreator.create("TopicTwoConsumer")(context.system)
          .mapAsync(1)(writeToElasticSearch) // call to write to elastic search
        .mapAsync(1)(processMessage) // just for logging
          .map(_.committableOffset)
        .toMat(Sink.ignore)(Keep.both) // sink ignore
        .run()
  }

  // This is used just for logging purposes
  private def processMessage(msg: Message): Future[Message] = {
    cnt += 1

    log.info(s"Record Number ${cnt} from topic2: ${msg.record.value()}")
    Future.successful(msg)
  }

  /**
    * This method uses the elastic search configuration mentioned in the application.conf
    * creates and Http outgoing connection
    * uses via flow to write to elastic search
    */
  def writeToElasticSearch(msg: Message): Future[Message] ={
    implicit val system = ActorSystem("SimpleSystem")

    // outgoing Http connection setup
    val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
      Http().outgoingConnection(config.getString("elasticSearch.url"), config.getInt("elasticSearch.port"))

    // via flow to write to elastic search
    def ipApiRequest(request: HttpRequest): Future[HttpResponse] =
      akka.stream.scaladsl.Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

    // method to post the Json string to elastic search
    def fetchIpInfo(jsonObj: String): Unit = {
      println(jsonObj)

      // Posts to the elastic search url mentioned in the application.conf
      val request = HttpRequest(POST, uri = config.getString("apiUrl.path"), entity = HttpEntity(ContentTypes.`application/json`,ByteString(jsonObj)))

      // for loogging reasons
      println(request)
      ipApiRequest(request).onComplete(
        status =>
          println(status)
      )
    }

    // conversion to Json String
    val jsonStr = processToJson(msg.record.value())

    // Calling the fetchIpInfo method to write to the elastic search
    println("calling fetchIpInfo")
    fetchIpInfo(jsonStr)
    println("call done")

    Future.successful(msg)
  }

  /**
    * This method uses the liftweb json package to convert the messages
    * using JsonCustomFormat to Json String
    */
  def processToJson(strMsg: String): String = {
    val msgArr = strMsg.split(",",6)

    //val msgArr = strMsg.split(/(?>"(?>\\.|[^"])*?"|(,))/, 6)

    val classObj = JsonCustomFormat(msgArr(0),msgArr(1),msgArr(2),msgArr(3),msgArr(4))

    implicit val formats = DefaultFormats

    val jsonString = write(classObj)

    jsonString
  }

}