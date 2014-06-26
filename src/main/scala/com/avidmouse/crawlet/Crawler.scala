package com.avidmouse.crawlet

import scala.concurrent.Future

import org.slf4j.LoggerFactory

import concurrent.ExecutionContext.Implicits.global

/**
 * @author avidmouse
 * @version 0.1, 14-6-26
 */
trait Crawler {

  import Crawler.Retry

  protected val logger = LoggerFactory.getLogger(getClass)

  val executionContext: concurrent.ExecutionContextExecutorService

  val result: Result

  val retryTimes = 3

  protected val taskCount = new java.util.concurrent.atomic.AtomicInteger(0)

  private def countDown() {
    if (taskCount.decrementAndGet() == 0) {
      logger.trace("Crawler job finished, be shutdown!")
      stop()
    }
  }

  private[crawlet] def execute(path: Path, req: Path => concurrent.Future[_]) {
    taskCount.incrementAndGet()
    concurrent.Future()(executionContext).flatMap(_ => req(path)).onComplete {
      case util.Success(v) =>
        result success(path.current, v)
        countDown()
      case util.Failure(t) =>
        req match {
          case retry@Retry(times, _) if times < retryTimes =>
            logger.trace(s"Crawler execute Retry [$times] times for path:$path failed;cause:", t)
            execute(path, retry.copy(times = times + 1))
          case Retry(_, _) =>
            result failure(path.current, t)
          case _ =>
            if (retryTimes == 1)
              result failure(path.current, t)
            else
              execute(path, Crawler.Retry(1, req))
        }
        countDown()
    }
  }

  val root = Path(this, Seq.empty[String])

  def isFinished = taskCount.get() == 0

  def stop() {
    executionContext.shutdown()
    System.exit(0)
  }

}

object Crawler {

  case class Retry(times: Int, req: Path => concurrent.Future[_]) extends (Path => concurrent.Future[_]) {
    override def apply(path: Path): Future[_] = req(path)
  }

}