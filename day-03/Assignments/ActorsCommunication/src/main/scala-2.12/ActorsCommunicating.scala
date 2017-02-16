import akka.actor._
import akka.actor.Props

/**
  * Created by anike_000 on 2/15/2017.
  */

abstract class CountEvent
case class StartCounting(n: Int, otherActor: ActorRef) extends CountEvent
case class CountDown(n: Int) extends CountEvent

object ActorsCommunicating extends App{

  class MyActor extends Actor{
    override def receive = {
      case StartCounting(n, otherActor) =>
        println(n)
        otherActor ! CountDown(n-1)

      case CountDown(n) =>
        if(n>0){
          println(n)
          sender ! CountDown(n-1)
        }
        else{
          context.system.terminate
        }
    }

  }

  val context = ActorSystem("SimpleSystem")
  val actor1 = context.actorOf(Props[MyActor], "actor1")
  val actor2 = context.actorOf(Props[MyActor], "actor2")

  actor1 ! StartCounting(10, actor2)
}
