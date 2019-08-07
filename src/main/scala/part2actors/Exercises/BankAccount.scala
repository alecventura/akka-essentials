package part2actors.Exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BankAccount extends App {

  val system = ActorSystem("ActorSystem")

  object BankAccountActor{
    case class Deposit(amount: Int)
    case class WithDraw(amount: Int)
  }

  class BankAccountActor extends Actor {
    import BankAccountActor._
    import StatementActor._
    var balance: Int = 0
    override def receive: Receive = {
      case Deposit(amount) =>
        val previousAmount = balance
        balance += amount
        statementActor ! Success(previousAmount, balance, self)

      case WithDraw(amount) =>
        if (balance >= amount){
          val previousAmount = balance
          balance -= amount
          statementActor ! Success(previousAmount, balance, self)
        } else {
          statementActor ! FailWithDraw(amount, balance, self)
        }
    }
  }

  object StatementActor {
    case class Success(previousAmount: Int, currentAmount: Int, ref: ActorRef)
    case class FailWithDraw(withDrawAmount: Int, currentAmount: Int, ref: ActorRef)
  }

  class StatementActor extends Actor {
    import StatementActor._
    override def receive: Receive = {
      case Success(previousAmount, currentAmount, ref) => println(ref, s"Amount successfully updated from $previousAmount to $currentAmount")
      case FailWithDraw(withDrawAmount, currentAmount, ref) => println(ref, s"Could not withdraw $withDrawAmount because there is only $currentAmount on your bank!")
    }
  }

  val bankAccountActor = system.actorOf(Props[BankAccountActor], "BankAccountActor")
  val statementActor = system.actorOf(Props[StatementActor], "StatementActor")

  import BankAccountActor._
  bankAccountActor ! Deposit(200)
  bankAccountActor ! Deposit(200)
  bankAccountActor ! WithDraw(2000)
  bankAccountActor ! WithDraw(50)
}
