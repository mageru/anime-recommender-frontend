package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import play.api.Routes
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.libs.json.Json
import models.Show
import models.UserScore

case class Recommendation(line: String) {
  val data = line.split(",")
  val showID : Int = data(0).toInt
  val strength : Double = data(1).toDouble
  val show : Show = Show.findById(showID).getOrElse(throw new IllegalArgumentException("Show not found"))
}


object RecommendationController extends Controller {

  
  def list() = Action { implicit request =>
      val showList: List[Show] = Show.getAllWithParser
      Ok(views.html.shows(showList))
  }
  
  def getShow(showid: Int) = Action { implicit request =>
     Show.findById(showid).map { show =>
      Ok(views.html.show(show))}.getOrElse(NotFound)
  }


  def getRecomendations(profile: String) = Action { implicit request =>
    val howMany: Option[String] = request.getQueryString("howMany")
  	val responsePromise = WS.url("http://csprofessional.net:8091/recommend/"+profile+"?howMany="+howMany.getOrElse("18")).get
  	val response = Await.result(responsePromise, 10 seconds);
  	
  	val recommendationsString = (response.body).split("\n") map (line => Recommendation(line))  	
  	val title = s"Recommendations based on profile: '$profile'"
  	Ok(views.html.recommendations(title,profile,recommendationsString))
  }
  
  def getWhy(profile:String, showID: Int) = Action { implicit request => 
    val responsePromise = WS.url("http://csprofessional.net:8091/because/"+profile+"/"+showID).get
  	val response = Await.result(responsePromise, 10 seconds);
  	
  	val recommendationsString = (response.body).split("\n") map (line => Recommendation(line))
  	val influencerShow : Show = Show.findById(showID).getOrElse(throw new IllegalArgumentException("Show not found"))
  	val showTitle = influencerShow.title
  	val title = s"Influencers for recommendation of '$showTitle' for $profile"
  	Ok(views.html.recommendations(title,"", recommendationsString))
  }
  
  def getRankings(username: String) = Action { implicit request => 
  	val rankings = UserScore.getProfileRankings(username)
  	Ok(views.html.rankings(rankings))
  }
  

}
