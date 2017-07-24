package models

import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile


case class Blog(
                 id:String,
                 name:String,
                 alias:String,
                 description:String,
                 image:Option[String],
                 logo:Option[String],
                 url:Option[String],
                 useAvatarAsLogo:Option[Boolean],
                 disqus:Option[String],
                 googleAnalytics:Option[String],
                 status:Int, //
                 owner:String,
                 twitter:Option[String],
                 showAds:Option[Boolean],
                 adsCode:Option[String]
               ) extends Identifiable

