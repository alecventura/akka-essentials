package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there!" // replying to a message
      case message: String => println(s"[simple actor] I have received $message")
      case number: Int => println(s"I have received a NUMBER: $number")
      case SpecialMethod(contents) => println(s"I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s")
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hello, actor "

  // 1 - messages can be of any type: under two conditions
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  //    -> in practice use case classes and case objects
  simpleActor ! 42

  case class SpecialMethod(contents: String)
  simpleActor ! SpecialMethod("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self === self === `this` in OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I'm proud of it")

  // 3 - actor can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!" // reply to actorSystem

  // 5 - forwarding messages

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)

  /**
   * Exercises
   *
   */



}
