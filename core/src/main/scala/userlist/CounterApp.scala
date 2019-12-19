package userlist

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile.backend.Database

import userlist.dao.CounterDao
import userlist.eventhandlers._

object CounterApp extends HttpApp with App {
  implicit val system: ActorSystem = ActorSystem("userlist", ConfigFactory.load)
  implicit val ec: ExecutionContext = system.dispatcher

  val readJournal = PersistenceQuery(system)
    .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  val counterRegion = ClusterSharding(system).start(
    CounterHandler.ShardName,
    Counter.props,
    ClusterShardingSettings(system),
    CounterHandler.extractEntityId,
    CounterHandler.extractShardId
  )

  val counterHandler = system.actorOf(CounterHandler.props(counterRegion))

  val db = Database.forConfig("db", system.settings.config)
  val counterDao = CounterDao(db)

  system.actorOf(CounterCreatedHandler.props(readJournal, counterDao))
  system.actorOf(CounterIncrementedHandler.props(readJournal, counterDao))
  system.actorOf(CounterDecrementedHandler.props(readJournal, counterDao))

  override def routes: Route = CounterRoutes.routes(counterHandler, counterDao)

  startServer("0.0.0.0", 8080, system)
}
