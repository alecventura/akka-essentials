package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  // Actor can create other actors


  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child $name")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))


    }


  }


  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  val system = ActorSystem("ActorSystem")

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("bob")
  parent ! TellChild("Sit!")

  // actor hierarchies
  // parent -> child -> grandChild ....
  //        -> child2 ->

  /*
  * Guardian actors (top-level)
  * - /system = system guardian
  * - /user = user-level guardian
  * - / = the root guardian
  * */

  /*Actor selection
  * */
  val childSelection = system.actorSelection("/user/parent/bob")
  childSelection ! "I found u"

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THIS `THIS` REFERENCE, TO CHILD ACTORS.
   * */

}
