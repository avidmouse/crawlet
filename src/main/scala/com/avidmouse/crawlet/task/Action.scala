package com.avidmouse.crawlet.task

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
trait Action[T] {

  def id: Int

  def parse: HttpResponse => Future[T]

  def exec(fetch: Fetch, parsed: T, taskRef: ActorRef)

  def apply(fetch: Fetch, resp: HttpResponse, taskRef: ActorRef)(implicit ec: ExecutionContext): Unit = resp.status match {
    case StatusCodes.OK =>
      parse(resp).foreach { parsed =>
        exec(fetch, parsed, taskRef)
        taskRef ! Fetch.Success(fetch.uri)
      }
    case status =>
      taskRef ! Fetch.Failure(fetch.uri, status)
  }

}

class Spawn(val id: Int, val parse: Spawn.Parse, act: Action[_]) extends Action[Seq[String]] {

  override def exec(fetch: Fetch, uris: Seq[String], taskRef: ActorRef) {
    uris.foreach { uri =>
      taskRef ! Fetch(uri, act)
    }
  }
}

object Spawn {
  type Parse = HttpResponse => Future[Seq[String]]

  def apply(id: Int, parse: Parse)(act: Action[_]): Spawn = new Spawn(id, parse, act)
}

class Map(val id: Int, val parse: Map.Parse, act: Action[_]) extends Action[String] {
  override def exec(fetch: Fetch, uri: String, taskRef: ActorRef) {
    taskRef ! Fetch(uri, act)
  }
}

object Map {
  type Parse = HttpResponse => Future[String]

  def apply(id: Int, parse: Parse)(act: Action[_]): Map = new Map(id, parse, act)
}
