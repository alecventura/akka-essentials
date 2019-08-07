package part2actors.Exercises

import akka.actor.{Actor, ActorSystem, Props}

object IncrementDecrement extends App {

  val system = ActorSystem("ActorSystem")

  // DOMAIN of IncrementDecrementActor
  object IncrementDecrementActor{
    case class Increment(number: Int)
    case class Decrement(number: Int)
    case object Print
  }


  class IncrementDecrementActor extends Actor {
    import IncrementDecrementActor._
    var count = 0
    override def receive: Receive = {
      case Increment(number) => count += number
      case Decrement(number) => count -= number
      case Print => println(self, count)
    }
  }

  val actor = system.actorOf(Props[IncrementDecrementActor], "IncrementDecrementActor")

  import IncrementDecrementActor._
  actor ! Increment(30)
  actor ! Print
}
