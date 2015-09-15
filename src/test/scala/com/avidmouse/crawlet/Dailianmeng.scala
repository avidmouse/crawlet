package com.avidmouse.crawlet

import akka.http.scaladsl.model.HttpResponse

import akka.http.scaladsl.unmarshalling.Unmarshal

import org.jsoup.Jsoup

import scala.collection.convert.wrapAsScala._

import task._

/**
 * @author avid mouse
 * @version 1.0, 09/15/2015
 */
object Dailianmeng extends Crawlet {

  val LastPage = """.*P2pBlacklist_page=(\d+).*""".r

  val indexParse: Spawn.Parse = { resp =>
    Unmarshal(resp.entity).to[String].map(Jsoup.parse).map { doc =>
      println("Last page url is: " + doc.body().select(".container .table-responsive .pagination .last a").attr("href"))
      doc.body().select(".container .table-responsive .pagination .last a").attr("href") match {
        case LastPage(last) =>
          for (p <- 1 to last.toInt.min(2)) yield s"http://www.dailianmeng.com/p2pblacklist/index.html?P2pBlacklist_page=$p&ajax=yw0"
        case _ => Seq.empty
      }
    }
  }

  val tableParse: Spawn.Parse = { resp =>
    Unmarshal(resp.entity).to[String].map(Jsoup.parse).map { doc =>
      for (a <- doc.body().select(".container .table-responsive .items tbody tr td a"))
        yield s"http://www.dailianmeng.com${a.attr("href")}"
    }
  }

  val detailParse = { resp: HttpResponse =>
    Unmarshal(resp.entity).to[String].map(Jsoup.parse).map { doc =>
      val t = doc.select(".detail-view tbody tr")
      def v(idx: Int) = t.get(idx).select("td").text()

      val eid = v(0)
      val mobile = v(1)
      val email = v(2)
      val qq = v(3)
      val name = v(4)
      (eid, mobile, email, qq, name)
    }
  }

  override def config = Task.Config("贷联盟黑名单", 1, Some(2000))

  start(Fetch("http://www.dailianmeng.com/p2pblacklist/index.html",
    Spawn(indexParse) {
      Spawn(tableParse) {
        Save(detailParse)
      }
    }
  ))

}

