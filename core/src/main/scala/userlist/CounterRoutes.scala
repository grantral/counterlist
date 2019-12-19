package userlist

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import userlist.Counter._
import userlist.CounterHandler._
import userlist.dao.CounterDao

object CounterRoutes {
  implicit val timeout: Timeout = 5.seconds

  private def try2Route[T](
      `try`: Try[T]
  )(f: T => Route): Route = `try` match {
    case Success(value) => f(value)
    case _              => complete(StatusCodes.InternalServerError)
  }

  private def tryOption2Route[T](
      `try`: Try[Option[T]]
  )(f: T => Route): Route = `try` match {
    case Success(value) =>
      value match {
        case Some(value) => f(value)
        case _           => complete(StatusCodes.InternalServerError)
      }
    case _ => complete(StatusCodes.InternalServerError)
  }

  private def try2RouteAndThen[T](
      future: Future[T]
  )(f: T => Route): Route =
    onComplete { future } {
      try2Route(_)(f)
    }

  private def tryOption2RouteAndThen[T](
      future: Future[Option[T]]
  )(f: T => Route): Route =
    onComplete { future } {
      tryOption2Route(_)(f)
    }

  def routes(counterHandler: ActorRef, counterDao: CounterDao): Route =
    path("counters") {
      get {
        try2RouteAndThen { counterDao.findAll } {
          complete(_)
        }
      } ~ post {
        val id = UUID.randomUUID
        onComplete { counterHandler ? EntityEnvelope(id, Create(id)) } {
          case Success(counter: Created) => complete(counter)
          case _                         => complete(StatusCodes.InternalServerError)
        }

        // FIXME: This doesn't work because circe cannot encode `Any`.
        // FIXME: Possible solution: explicitly specify a type.
        /*try2RouteAndThen { counterHandler ? EntityEnvelope(id, Create(id)) } {
            complete(_)
          }*/
      }
    } ~ pathPrefix("counter" / JavaUUID) { id =>
      path("inc") {
        post {
          tryOption2RouteAndThen { counterDao.findOne(id) } { _ =>
            counterHandler ! EntityEnvelope(id, Increment(id))
            complete(StatusCodes.OK)
          }
        }
      } ~ path("dec") {
        post {
          tryOption2RouteAndThen { counterDao.findOne(id) } { _ =>
            counterHandler ! EntityEnvelope(id, Decrement(id))
            complete(StatusCodes.OK)
          }
        }
      } ~ get {
        tryOption2RouteAndThen { counterDao.findOne(id) } {
          complete(_)
        }
      }
    }
}
