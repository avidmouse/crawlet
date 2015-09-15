package com.avidmouse.crawlet.task

import akka.actor.{Actor, ActorLogging, Props}

/**
 * @author avid mouse
 * @version 2.0, 09/15/2015
 */
object Task {

  case class Config(name: String, maxThreads: Int, interval: Option[Long] = None)

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
        log.debug("Fetcher start Fetch {}", uri)
        Http().singleRequest(HttpRequest(uri = uri)).foreach { r =>
          log.debug("Fetcher exec act {}", uri)
          act(f, r, task)
        }
    }

  }

}

class Task(conf: Task.Config) extends Actor with ActorLogging {

  import akka.routing.RoundRobinPool

  import scala.util.{Failure, Success}

  var count = 0

  val fetchers = context.actorOf(RoundRobinPool(conf.maxThreads).props(Props(classOf[Task.Fetcher], conf)), "fetchRouter")

  private def countDown(): Unit = {
    count -= 1
    log.debug("Task count now is:" + count)
    if (count == 0) {
      log.debug("Task Succeed")
      context.system.terminate()
    }
  }

  def receive = {
    case f: Fetch =>
      log.debug("Task receive fetch {}", f.uri)
      count += 1
      fetchers ! f
    case Success(url) =>
      log.debug("Task receive Success:" + url)
      countDown()
    case Failure(failure) =>
      log.debug("Task receive Failure:" + failure.getMessage)
      countDown()
  }

}
