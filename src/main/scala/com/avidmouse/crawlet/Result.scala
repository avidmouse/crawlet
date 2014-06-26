package com.avidmouse.crawlet

/**
 * @author avidmouse
 * @version 0.1, 14-6-26
 */
trait Result {

  def success(path: Seq[String], result: Any)

  def failure(path: Seq[String], cause: Throwable)
}

object Result {

  class SystemOutResult extends Result {
    override def success(path: Seq[String], result: Any) {
      println("Success:" + path.mkString("/") + ";result:" + result)
    }

    override def failure(path: Seq[String], cause: Throwable) {
      print("Failure:" + path.mkString("/") + ";cause:")
      cause.printStackTrace(System.out)
    }
  }

  class Slf4jResult extends Result {

    import org.slf4j.LoggerFactory

    val logger = LoggerFactory.getLogger(classOf[Slf4jResult])

    override def success(path: Seq[String], result: Any) {
      logger.info("Success:" + path.mkString("/") + ";result:" + result)
    }

    override def failure(path: Seq[String], cause: Throwable) {
      logger.info("Failure:" + path.mkString("/") + ";cause:", cause)
    }

  }

}