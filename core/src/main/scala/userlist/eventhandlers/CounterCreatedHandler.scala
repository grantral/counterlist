package userlist.eventhandlers

import akka.actor.{ActorLogging, Props}
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, Sequence}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import userlist.dao.CounterDao
import userlist.dao.CounterTable.CounterEntity
import userlist.{CborSerializable, Counter}

object CounterCreatedHandler {
  sealed trait Command extends CborSerializable
  final case object Init extends Command
  final case object Ack
  final case object Completed extends Command
  final case class Failure(e: Throwable) extends Command

  sealed trait Event extends CborSerializable
  final case class CreatedHandled(offset: Long) extends Event

  final case class State(offset: Long) extends CborSerializable {
    def update(event: Event): State = event match {
      case CreatedHandled(offset) => copy(offset)
    }
  }

  def props(
      readJournal: JdbcReadJournal,
      counterDao: CounterDao
  )(implicit mat: Materializer): Props =
    Props(new CounterCreatedHandler(readJournal, counterDao))
}

class CounterCreatedHandler(
    readJournal: JdbcReadJournal,
    counterDao: CounterDao
)(implicit mat: Materializer)
    extends PersistentActor
    with ActorLogging {
  import CounterCreatedHandler._

  private var state = State(0L)

  private def updateState(event: Event): Unit =
    state = state.update(event)

  override val persistenceId: String = "CounterCreatedHandler"

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case RecoveryCompleted =>
      readJournal
        .eventsByTag("counter-created", state.offset)
        .runWith(
          Sink.actorRefWithBackpressure(self, Init, Ack, Completed, Failure)
        )
  }

  override def receiveCommand: Receive = {
    case cmd: Command =>
      cmd match {
        case Init       => sender() ! Ack
        case Completed  => context.stop(self)
        case Failure(e) => log.error(e.getMessage)
      }

    case EventEnvelope(offset, _, _, Counter.Created(id)) =>
      offset match {
        case Sequence(value) =>
          counterDao.save(CounterEntity(id))
          persist(CreatedHandled(value))(updateState)
          sender() ! Ack
      }
  }
}
