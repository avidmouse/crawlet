package com.avidmouse.crawlet

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
trait Crawlet extends App {

  import akka.actor.{Props, ActorSystem}
  import akka.stream.ActorMaterializer

  import com.avidmouse.crawlet.task.Task

  implicit val system = ActorSystem("Crawlet")

  implicit val executionContext = system.dispatcher

  implicit val materializer = ActorMaterializer()

  def config: Task.Config

  val taskRef = system.actorOf(Props(classOf[task.Task], config), "task")

  def start(root: task.Fetch) {
    taskRef ! root
  }

}