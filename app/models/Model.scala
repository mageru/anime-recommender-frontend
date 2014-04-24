package models
import play.api.mvc.{Action}
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.parsing.combinator.RegexParsers
/**
 * Created by Justin
 */
case class Show(showid: Integer, title: String, status: String, classification: String,
                episodes: Option[Int], showType: String, startDate: Option[String],
                endDate: Option[String], avgMemberScore: String, genres: String)

object Show {
  import anorm.SQL
  import anorm.SqlQuery

  val sql: SqlQuery = SQL("""select s.showid as showid,s.title,s.status,s.classification,s.episodes,s.showtype,s.startdate,s.enddate,s.averagememberscore, '#' || string_agg(g.genre,' #') as genres
                            from preferences.shows s, preferences.genres g 
                            where s.showid = g.showid
                            and g.genre != ''
                            group by s.showid,s.title,s.status,s.classification,s.episodes,s.showtype,s.startdate,s.enddate
                            ORDER BY s.showid""".stripMargin)


  import play.api.Play.current
  import play.api.db.DB
  
  def findById(showid: Int): Option[Show] = {
      DB.withConnection { implicit connection =>
        val sql = SQL("""select s.showid ,s.title,s.status,s.classification,s.episodes,s.showtype,s.startdate,s.enddate, s.averagememberscore,'#' || string_agg(g.genre,' #') as genres
                            from preferences.shows s, preferences.genres g 
                            where s.showid = g.showid
                            and g.genre != ''
                            and s.showid = {showid}
                            group by s.showid,s.title,s.status,s.classification,s.episodes,s.showtype,s.startdate,s.enddate
                            ORDER BY s.showid""".stripMargin)
        sql.on("showid" -> showid).as(showParser.singleOpt)
      }
  }

  def getAll: List[Show] = DB.withConnection { 
    implicit connection =>

    sql().map ( row =>
      Show(row[Int]("showid"), row[String]("title"),
        row[String]("status"), row[String]("classification"),
        row[Option[Int]]("episodes"), row[String]("showType"),
        row[Option[String]]("startdate"), row[Option[String]]("enddate"),
        row[String]("averagememberscore"), row[String]("genres"))
    ).toList    
  }

  import anorm.RowParser

  val showParser: RowParser[Show] = {
    import anorm.~
    import anorm.SqlParser._

    int("showid") ~ 
    str("title") ~ 
    str("status") ~ 
    str("classification") ~ 
    get[Option[Int]]("episodes") ~ 
    str("showtype") ~ 
    get[Option[String]]("startdate") ~ 
    get[Option[String]]("endDate") ~ 
    str("averagememberscore") ~
    str("genres") map {
      case showid ~ title ~ status ~ classification ~ episodes ~ showType ~ startDate ~ endDate ~ avgMemberScore ~ genres =>
        Show(showid, title, status, classification,
            episodes, showType, startDate,
            endDate, avgMemberScore, genres)
    }
  }
  
  import anorm.ResultSetParser

  val showsParser: ResultSetParser[List[Show]] = {
     showParser *
  }
  
  def getAllWithParser: List[Show] = DB.withConnection {
    implicit connection =>
    sql.as(showsParser)
  }
}  


