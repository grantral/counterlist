package userlist

import akka.persistence.journal.{EventAdapter, EventSeq, Tagged}

import userlist.Counter._

class TaggingEventAdapter extends EventAdapter {
  override def manifest(event: Any): String = ""

  private def tag(event: Event, tag: String) = Tagged(event, Set(tag))

  override def toJournal(event: Any): Any = event match {
    case evt: Event =>
      evt match {
        case Created(id)     => tag(Created(id), "counter-created")
        case Incremented(id) => tag(Incremented(id), "counter-incremented")
        case Decremented(id) => tag(Decremented(id), "counter-decremented")
      }
  }

  override def fromJournal(event: Any, manifest: String): EventSeq =
    EventSeq.single(event)
}
