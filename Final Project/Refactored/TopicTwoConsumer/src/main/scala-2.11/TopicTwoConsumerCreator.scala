import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

/**
  * Created by anike_000 on 2/23/2017.
  * consumer Config object
  * specifies the topic to receive the messages from
  * sets the offset commit/reset properties
  */
object TopicTwoConsumerCreator {
  def create(groupId: String)(implicit system: ActorSystem): Source[ConsumerMessage.CommittableMessage[Array[Byte], String], Consumer.Control] = {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      //.withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true") // to commit the offsets

    Consumer.committableSource(consumerSettings, Subscriptions.topics(TopicDefinition.TOPIC2))
  }

}
