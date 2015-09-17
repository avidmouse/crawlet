package com.avidmouse.crawlet

import akka.actor.{Props, ActorSystem}
import akka.stream.ActorMaterializer

import com.avidmouse.crawlet.task.Task

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
trait Crawlet {

  implicit val system = ActorSystem("Crawlet")

  implicit val executionContext = system.dispatcher

  implicit val materializer = ActorMaterializer()

  def createTask(config: Task.Config) = system.actorOf(Props(classOf[task.Task], config))

}
