package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogsSpec extends TestKit(ActorSystem("InterceptingLogsSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
{
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case class DispatchOrder(item: String)
  case object OrderConfirmed

  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])
    override def receive: Receive = awatingCheckout


    def awatingCheckout: Receive = {
      case Checkout(item, card) =>
        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }


    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed => context.become(awatingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if (card.startsWith("0")) sender() ! PaymentDenied
      else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor {
    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1
        sender() ! OrderConfirmed
    }
  }
}
