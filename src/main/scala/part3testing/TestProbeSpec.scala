package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "send the work to slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoadString = "I love akka"
      master ! Work(workLoadString)

      // the interaction between the master and the slave actor
      slave.expectMsg(SlaveWork(workLoadString, testActor))
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3)) // testActor receives the Report(3)
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoadString = "I love akka"
      master ! Work(workLoadString)
      master ! Work(workLoadString)

      // in the meantime I don't have a slave actor
      slave.receiveWhile() {
        case SlaveWork(`workLoadString`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
      }
      expectMsg(Report(3))
      expectMsg(Report(6))
    }
  }
}

object TestProbeSpec {
  case class Register(slaveRef: ActorRef)
  case class SlaveWork(text: String, originalRequester: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Work(text: String)
  case class Report(totalCount: Int)
  case object RegistrationAck


  class Master extends Actor {

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }

    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ => //ignore
    }
  }

//  class Slave extends Actor {
//    override def receive: Receive = {
//
//    }
//  }
}