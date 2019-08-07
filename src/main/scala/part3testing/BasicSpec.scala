package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._
  "An simple echo actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello"
      echoActor ! message
      expectMsg(message)
    }

    "do that" in {
      // test 2
    }
  }

  "A blackhole actor" should {
    "send back some messsage" in {
      val blackhole = system.actorOf(Props[Blackhole])
      val message = "hello"
      blackhole ! message
      expectNoMessage(1 second)
    }
  }

  // message assertions
  "A LabTestActor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into upper case" in {
      labTestActor ! "love"
      val reply = expectMsgType[String] // can get the message this way

      assert(reply == "LOVE")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply to a favoriteTech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply to a favoriteTech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) // Seq[Any]

      assert(messages.contains("Scala"))
    }

    "reply to a favoriteTech in a fancy way" in {
      labTestActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" => // only care that the partial function is defined
        case "Akka" =>
      }
    }
  }
}


object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class Blackhole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }
}