package tools

import controllers.routes
import java.text.Normalizer

import models.{Blog, Post}
import org.joda.time.{LocalDateTime, Period}
import play.api.i18n.Messages

import scala.annotation.tailrec
import scravatar.Gravatar

object PostAux  {

  val defaultPageSize = 10

  private[this] val slash = "/"

  def canonical(blog:Blog, post:Post): String = {
    val base = blog.url.getOrElse("prosa.canonical.url")
    if (base.endsWith(slash))
      base.stripSuffix(slash) + slug(blog.alias, post, drafts=false)
    else
      base + slash + slug(blog.alias, post, drafts=false)
  }

  // build slugs for links
  def slug(blog: String, post: Post, drafts:Boolean): String = {
    val blogDate = if (drafts) post.created else post.published
    blogDate match {
      case None => routes.PostsController.edit(blog, post.id).url
      case Some(d) => buildSlug(blog, d, post, drafts)
    }
  }

  private[this] def buildSlug(alias: String, d: LocalDateTime, post: Post, draft:Boolean): String = {
    val date = new LocalDateTime(d)
    if (draft)
      routes.PostsController.edit(alias, post.id).url
    else
      routes.PostsGuestController.view(alias, date.getYear, date.getMonthOfYear, date.getDayOfMonth, post.slug.get).url
  }

  def atomUrl(urlBase:String): String = {
    if (urlBase.endsWith(slash))
      urlBase + "atom.xml"
    else
      urlBase + "/atom.xml"
  }

  val excerptSize = 250

  def excerpt(content: String) : Option[String] =
      Option(org.jsoup.Jsoup.parse(content).text()) flatMap { txt =>
        val exc = txt.take(excerptSize)
        if (exc.length < txt.length)
          Some(exc + "...")
        else
          Some(exc)
      }

  def slugify(plainText:String) : Option[String] =
    Option(plainText) flatMap { str =>
      Some(Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\w ]", "").replace(" ", "-").toLowerCase())
    }

  @tailrec
  def generateUniqueSlug(slug: String, existingSlugs: Seq[String]): String = {
    if (!(existingSlugs contains slug)) {
      slug
    } else {
      val endsWithNumber = "(.+-)([0-9]+)$".r
      slug match {
        case endsWithNumber(s, n) => generateUniqueSlug(s + (n.toInt + 1), existingSlugs)
        case _ => generateUniqueSlug(slug + "-2", existingSlugs)
      }
    }
  }

  def avatarUrl(email:String) : Option[String]=
    Option(Gravatar(email).ssl(true).avatarUrl)

  def extractDomain(url:String): String = {
    val u = new java.net.URL(url)
    u.getHost
  }





  def formatElapsed(date:Option[LocalDateTime])(implicit messages:Messages): String = {

    date match {
      case None => ""
      case Some(base) =>
        val now = new LocalDateTime()
        val period = new Period(base, now)
        namePeriod(period)

    }
  }

  private[this] def namePeriod(period: Period) : String = {
    lazy val year = Messages("dates.year")
    lazy val years = Messages("dates.years")
    lazy val month = Messages("dates.month")
    lazy val months = Messages("dates.months")
    lazy val week = Messages("dates.week")
    lazy val weeks = Messages("dates.weeks")
    lazy val day = Messages("dates.day")
    lazy val days = Messages("dates.days")
    lazy val hour = Messages("dates.hour")
    lazy val hours = Messages("dates.hours")
    lazy val minute = Messages("dates.minute")
    lazy val minutes = Messages("dates.minutes")
    lazy val second = Messages("dates.second")
    lazy val seconds = Messages("dates.seconds")
    if (period.getYears > 1)
      s"${period.getYears} $years"
    else if (period.getYears == 1)
      s"${period.getYears} $year"
    else if (period.getMonths > 1)
      s"${period.getMonths} $months"
    else if (period.getMonths == 1)
      s"${period.getMonths} $month"
    else if (period.getWeeks > 1)
      s"${period.getWeeks} $weeks"
    else if (period.getWeeks == 1)
      s"${period.getWeeks} $week"
    else if (period.getDays > 1)
      s"${period.getDays} $days"
    else if (period.getDays == 1)
      s"${period.getDays} $day"
    else if (period.getHours > 1)
      s"${period.getHours} $hours"
    else if (period.getHours == 1)
      s"${period.getHours} $hour"
    else if (period.getMinutes > 1)
      s"${period.getMinutes} $minutes"
    else if (period.getMinutes == 1)
      s"${period.getMinutes} $minute"
    else if (period.getSeconds > 1)
      s"${period.getSeconds} $seconds"
    else
      s"${period.getSeconds} $second"
  }

}
