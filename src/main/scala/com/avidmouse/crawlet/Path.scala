package com.avidmouse.crawlet

/**
 * @author avidmouse
 * @version 0.1, 14-6-26
 */
case class Path(crawler: Crawler, current: Seq[String]) {
  def /(path: String) = copy(current = current :+ path)

  def by(req: Path => concurrent.Future[_]) = {
    crawler.execute(this, req)
  }
}