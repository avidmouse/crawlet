package com.avidmouse.crawlet.task

import akka.actor.{ActorRef, Actor, ActorLogging, Props}

/**
 * @author avid mouse
 * @version 2.0, 09/15/2015
 */
object Task {

  case class Config(name: String, maxThreads: Int, interval: Option[Long] = None, listener: Option[ActorRef] = None)

  case class Finished(nrOfFetch: Int)

  private final class Fetcher(config: Config) extends Actor with ActorLogging {

    import akka.http.scaladsl.Http
    import akka.http.scaladsl.model._
    import akka.stream.ActorMaterializer
    import context.{dispatcher, system}

    implicit val materializer = ActorMaterializer()(context)

    def receive = {
      case f@Fetch(uri, act) =>
        log.debug("Fetcher receive Fetch {}", uri)
        config.interval.foreach(Thread.sleep)
        val task = sender()
        Http().singleRequest(HttpRequest(uri = uri)).foreach { r =>
          log.debug("Fetcher exec act {}", uri)
          act(f, r, task)
        }
    }

  }

}

class Task(conf: Task.Config) extends Actor with ActorLogging {

  import akka.routing.RoundRobinPool

  var count = 0

  var nrOfFetch = 0

  val fetchers = context.actorOf(RoundRobinPool(conf.maxThreads).props(Props(classOf[Task.Fetcher], conf)), "fetchRouter")

  //start
  conf.listener.foreach(_ ! conf)

  private def countDown(): Unit = {
    count -= 1
    log.debug("Task count now is: {}", count)
    if (count == 0) {
      log.debug("Task Succeed")
      conf.listener.foreach(_ ! Task.Finished(nrOfFetch))
    }
  }

  def receive = {
    case f: Fetch =>
      count += 1
      nrOfFetch += 1
      log.debug("Task receive fetch {}: {}", nrOfFetch, f.uri)
      fetchers ! f
      conf.listener.foreach(_ !(f, nrOfFetch))
    case e@Fetch.Success(uri) =>
      log.debug("Task receive Success: {}", uri)
      countDown()
      conf.listener.foreach(_ ! e)
    case e@Fetch.Failure(uri, status) =>
      log.debug("Task receive Failure: {}, status: {}", uri, status)
      countDown()
      conf.listener.foreach(_ ! e)
  }

}
