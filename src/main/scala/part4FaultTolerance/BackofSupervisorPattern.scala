package part4FaultTolerance

import java.io.File

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import scala.concurrent.duration._
import scala.io.Source

object BackofSupervisorPattern extends App {

  case object ReadFile
  class FileBasedPersistentActor extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit = log.info("Persistent actor starting")

    override def postStop(): Unit = log.warning("Persistent actor has stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.warning("Persistent actor restarting")

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data.text"))
        log.info("I've just read some important data: " + dataSource.getLines().toList)
    }
  }

  val system = ActorSystem("BackoffSupervisorDemo")
//  val simpleActor = system.actorOf(Props[FileBasedPersistentActor], "SimpleActor")
//  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      Props[FileBasedPersistentActor],
      "simpleBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    )
  )

//  val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
//  simpleBackoffSupervisor ! ReadFile

  val stopSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[FileBasedPersistentActor],
      "stopBackoffActor",
      3 seconds,
      30 seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy(){
        case _ => Stop
      }
    )
  )

//  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
//  stopSupervisor ! ReadFile

  class EagerFBPActor extends FileBasedPersistentActor {
    override def preStart(): Unit = {
      log.info("EagerActor starting")
      dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_data2.text"))
    }
  }

  val eagerActor = system.actorOf(Props[EagerFBPActor])
  // default -> ActorInitializationException => STOP
  val repeatedSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[EagerFBPActor],
      "eagerActor",
      1 second,
      30 seconds,
      0.2
    )
  )
  val repeatedSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")
  /*
  - eagerSupervisor
    - creates a eagerActor
      - will die on start with ActorInitializationException
      - trigger the supervision strategy in eagerSupervisor => STOP eagerActor
    - backoff will kick in after 1 second, 2s, 4, 8, 16
   */

}
