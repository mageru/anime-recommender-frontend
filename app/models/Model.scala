package models

/**
 * Created by Justin on 4/23/2014.
 */
case class Show(showid: Integer, title: String, status: String, classification: String,
                episodes: Option[Int], showType: String, startDate: Option[String],
                endDate: Option[String], avgMemberScore: String)

object Show {
  import anorm.SQL
  import anorm.SqlQuery

  val sql: SqlQuery = SQL("select * from preferences.shows")

  import play.api.Play.current
  import play.api.db.DB

  def getAll: List[Show] = DB.withConnection { 
    implicit connection =>

    sql().map ( row =>
      Show(row[Int]("showid"), row[String]("title"),
        row[String]("status"), row[String]("classification"),
        row[Option[Int]]("episodes"), row[String]("showType"),
        row[Option[String]]("startdate"), row[Option[String]]("enddate"),
        row[String]("averagememberscore"))
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
    str("averagememberscore") map {
      case showid ~ title ~ status ~ classification ~ episodes ~ showType ~ startDate ~ endDate ~ avgMemberScore =>
        Show(showid, title, status, classification,
            episodes, showType, startDate,
            endDate, avgMemberScore)
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