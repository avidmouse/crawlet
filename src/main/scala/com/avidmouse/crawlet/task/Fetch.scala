package com.avidmouse.crawlet.task

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
case class Fetch(uri: String, act: Action[_])

object Fetch {

  class Failure(url: String, status: akka.http.scaladsl.model.StatusCode) extends Exception(s"fetch url $url return code: $status")

}
