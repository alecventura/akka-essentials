package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultiThreadingRecap extends App {


  // creating threads on the JVM

  val aThreadWithoutSugarSintax = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })

  val aThread = new Thread(() => println("I'm running in parallel"))
  aThread.start()
  aThread.join() // method to wait thread to finish

  // different runs produce different results

  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money

    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }


    // @volatile locks the variable so only one thread can use at same tipe (but only works for primitives)
    // .synchronized locks the method so only one thread can use at same
  }


  // inter-thread communication on the JVM
  // wait - notify mechanism

  // Scala futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation - on a different thread
    42
  }

  // callbacks
  future.onComplete {
    case Success(42) => println("I found the meaning of life")
    case Failure(_) => println("fail")
  }

  val aProcessedFuture = future.map(_ + 1)
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  }

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchElementExceptions

  // for comprehensions
  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // andThen, recover, recoverWith


  // Promises



}
