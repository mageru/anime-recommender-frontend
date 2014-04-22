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

case class Message(value: String)

object MessageController extends Controller {

  implicit val fooWrites = Json.writes[Message]

  def getMessage = Action {
    Ok(Json.toJson(Message("Hello from Scala")))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(routes.javascript.MessageController.feedTitle)).as(JAVASCRIPT)
  }
  
  def feedTitle() = Action { implicit request =>
  val profile: Option[String] = request.getQueryString("profile")
  Async {
        WS.url("http://csprofessional.net:8091/recommend/"+profile.getOrElse("mageru")).withHeaders("Accept"-> "application/json").get.map(response => Ok(response.body))
    }     
  }
  
  /**
  def feedTitle() = Action {
      val responseFuture = WS.url("http://ubuntu:8091/recommend/mageru").get()
      val resultFuture = responseFuture map { response => 
        response.status match {
            case 200 => Some(response)
            case _ => None
        }
      }
      val result = Await.result(resultFuture, 5 seconds)
      Ok(result.get)
  }
  **/

}
