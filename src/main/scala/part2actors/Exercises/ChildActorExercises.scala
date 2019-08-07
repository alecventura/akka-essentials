package part2actors.Exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorExercises extends App {

  // Distributed Word counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
    case object TellFinalCount
  }
  class WordCounterMaster extends Actor{
    import WordCounterMaster._
    import WordCounterWorker._

    def withWorkers(threads: Seq[ActorRef], workerIndex: Int, totalCountSoFar: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case WordCountTask(id, text) =>
        val originalSender = sender()
        threads(workerIndex) ! WorkerCountWords(id, text)
        val newRequestMap = requestMap + (id -> originalSender)
        context.become(withWorkers(threads , if(workerIndex + 1 > threads.length) 0 else workerIndex + 1, totalCountSoFar, newRequestMap))

      case WordCountReply(id, count) =>
        val originalSender = requestMap(id)
        originalSender ! count
        println(s"${self.path} - count so far is ${totalCountSoFar + count} ")
        context.become(withWorkers(threads, workerIndex, totalCountSoFar + count, requestMap - id))

      case TellFinalCount => println(s"Final count is $totalCountSoFar")
    }

    override def receive: Receive = {
      case Initialize(nChildren) =>
        var threads = Seq[ActorRef]()
        for (c <- 1 to nChildren) {
          val children = context.actorOf(Props[WordCounterWorker], c.toString)
          threads = threads :+ children
        }
        context.become(withWorkers(threads, 0, 0, Map()))
    }
  }

  object WordCounterWorker {
    case class WorkerCountWords(id: Int, text: String)
  }
  class WordCounterWorker extends Actor {
    import WordCounterWorker._
    import WordCounterMaster._
    override def receive: Receive = {
      case WorkerCountWords(id, text) =>
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")

        import WordCounterMaster._
        master ! Initialize(3)
        master ! WordCountTask(1, "a b c")
        master ! WordCountTask(2, "a b")
        master ! WordCountTask(3, "a b c d e")
        master ! TellFinalCount
      case count: Int =>
        println(s"I got $count")
    }
  }

  val system = ActorSystem("ActorSystem")
  val testActor = system.actorOf(Props[TestActor])
  testActor ! "go"
}
