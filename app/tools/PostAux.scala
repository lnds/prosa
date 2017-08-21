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

  private[this] def namePeriod(period: Period)(implicit messages:Messages) : String = {
    nameYears(period)
  }

  private[this] def nameYears(period: Period)(implicit messages:Messages) : String = {
    lazy val year = Messages("dates.year")
    lazy val years = Messages("dates.years")
    period.getYears match {
      case y if y > 1 => s"$y $years"
      case 1 => s"1 $year"
      case _ => nameMonths(period)
    }
  }

  private[this] def nameMonths(period: Period)(implicit messages:Messages) : String = {
    lazy val month = Messages("dates.month")
    lazy val months = Messages("dates.months")
    period.getMonths match {
      case m if m > 1 => s"$m $months"
      case 1 => s"1 $month"
      case _ => nameWeeks(period)
    }
  }

  private[this] def nameWeeks(period: Period)(implicit messages:Messages) : String = {
    lazy val week = Messages("dates.week")
    lazy val weeks = Messages("dates.weeks")
    period.getWeeks match {
      case w if w > 1 => s"$w $weeks"
      case 1 => s"1 $week"
      case _ => nameDays(period)
    }
  }

  private[this] def nameDays(period: Period)(implicit messages:Messages) : String = {
    lazy val day = Messages("dates.day")
    lazy val days = Messages("dates.days")
    period.getDays match {
      case d if d > 1 => s"$d $days"
      case 1 =>  s"1 $day"
      case _ => nameHours(period)
    }
  }


  private[this] def nameHours(period: Period)(implicit messages:Messages) : String = {
    lazy val hour = Messages("dates.hour")
    lazy val hours = Messages("dates.hours")
    period.getHours match {
      case h if h > 1 =>  s"$h $hours"
      case 1 =>  s"1 $hour"
      case _ => nameMinutes(period)
    }
  }


  private[this] def nameMinutes(period: Period)(implicit messages:Messages) : String = {
    lazy val minute = Messages("dates.minute")
    lazy val minutes = Messages("dates.minutes")
    period.getMinutes match {
      case m if m > 1 =>  s"$m $minutes"
      case 1 => s"1 $minute"
      case _ => nameSeconds(period)
    }
  }

  private[this] def nameSeconds(period: Period)(implicit messages:Messages) : String = {
    lazy val second = Messages("dates.second")
    lazy val seconds = Messages("dates.seconds")
    period.getSeconds match {
      case 1 => s"${period.getSeconds} $second"
      case s => s"$s $seconds"
    }
  }



}
