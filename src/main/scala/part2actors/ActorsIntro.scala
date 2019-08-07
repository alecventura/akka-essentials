package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // part1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  // word count actor

  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0

    //behavior
    // def receive: Receive = { // same thing
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part 4 - communicate!
  wordCounter ! "I am learning akka and it's pretty damn cool!" // exclamation method is also know as "tell"
  anotherWordCounter ! "a different message"
  // asynchronous!

  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

//  val person = actorSystem.actorOf(Props(new Person("bob")), "personBob") // create actors with arguments this way is legal but its discourage
  val person = actorSystem.actorOf(Person.props("bob"), "personBob") // best way is to create a companion object and use this way
  person ! "hi"



}
