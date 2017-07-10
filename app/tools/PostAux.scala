package tools

import controllers.routes
import java.text.Normalizer
import models.{Blog, Post}
import org.joda.time.{Period, DateTime}
import play.api.i18n.Messages
import scala.annotation.tailrec
import scravatar.Gravatar

object PostAux  {


  def canonical(blog:Blog, post:Post) = {
    val base = blog.url.getOrElse("prosa.canonical.url")
    if (base.endsWith("/"))
      base.stripSuffix("/") + slug(blog.alias, post, drafts=false)
    else
      base + "/" + slug(blog.alias, post, drafts=false)
  }

  // build slugs for links
  def slug(blog: String, post: Post, drafts:Boolean) = {
    val bdate = if (drafts) post.created else post.published
    bdate match {
      case None => routes.PostsController.edit(blog, post.id).url
      case Some(d) =>
        slugString(blog, d, post, drafts)
    }
  }

  def slugString(alias: String, d: DateTime, post: Post, draft:Boolean) = {
    val date = new DateTime(d)
    if (draft)
      routes.PostsController.edit(alias, post.id).url
    else
      routes.PostsGuestController.view(alias, date.getYear, date.getMonthOfYear, date.getDayOfMonth, post.slug.get).url
  }

  def atomUrl(urlBase:String) = {
    if (urlBase.endsWith("/"))
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
        case sl => generateUniqueSlug(sl + "-2", existingSlugs)
      }
    }
  }

  def avatarUrl(email:String) : Option[String]=
    Option(Gravatar(email).ssl(true).avatarUrl)

  def extractDomain(url:String) = {
    val u = new java.net.URL(url)
    u.getHost
  }




  def formatElapsed(date:Option[DateTime])(implicit messages:Messages): String = {
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
    date match {
      case None => ""
      case Some(base) =>
        val baseDate = new DateTime(base)
        val now = new DateTime()
        val period = new Period(baseDate, now)
        if (period.getYears >= 1)
          period.getYears.toString + " " + (if (period.getYears > 1) years else year)
        else if (period.getMonths >= 1)
          period.getMonths.toString + " " + (if (period.getYears > 1) months else month)
        else if (period.getWeeks >= 1)
          period.getWeeks.toString + " " + (if (period.getWeeks > 1) weeks else week)
        else if (period.getDays >= 1)
          period.getDays.toString + " " + (if (period.getDays > 1) days else day)
        else if (period.getHours >= 1)
          period.getHours.toString + " " + (if (period.getHours > 1) hours else hour)
        else if (period.getMinutes >= 1)
          period.getMinutes.toString + " " + (if (period.getMinutes > 1) minutes else minute)
        else
          period.getSeconds.toString + " " + (if (period.getSeconds > 1) seconds else second)
    }
  }

}
