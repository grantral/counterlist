package userlist

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

object Counter {
  sealed trait Command extends CborSerializable {
    val id: UUID
  }
  final case class Create(id: UUID) extends Command
  final case class Increment(id: UUID) extends Command
  final case class Decrement(id: UUID) extends Command

  sealed trait Event extends CborSerializable {
    val id: UUID
  }
  final case class Created(id: UUID) extends Event
  final case class Incremented(id: UUID) extends Event
  final case class Decremented(id: UUID) extends Event

  final case class State(count: Int) extends CborSerializable {
    def update(event: Event): State = event match {
      case Created(_)     => State(0)
      case Incremented(_) => copy(count + 1)
      case Decremented(_) => copy(count - 1)
    }
  }

  def props: Props = Props(new Counter)
}

class Counter extends PersistentActor with ActorLogging {
  import Counter._

  private var state = State(0)

  private def handleEvent(event: Event): Unit =
    state = state.update(event)

  override val persistenceId: String = s"counter-${self.path.name}"

  override def receiveRecover: Receive = {
    case event: Event => handleEvent(event)
  }

  override def receiveCommand: Receive = {
    case cmd: Command =>
      cmd match {
        case Create(id) =>
          val counter = Created(id)
          persist(counter) { e =>
            handleEvent(e)
            sender() ! counter
          }
        case Increment(id) => persist(Incremented(id))(handleEvent)
        case Decrement(id) => persist(Decremented(id))(handleEvent)
      }
  }
}
