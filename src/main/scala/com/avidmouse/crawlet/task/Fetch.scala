package com.avidmouse.crawlet.task

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
case class Fetch(uri: String, act: Action[_])

object Fetch {

  case class Success(uri: String)

  case class Failure(uri: String, status: akka.http.scaladsl.model.StatusCode)

}
