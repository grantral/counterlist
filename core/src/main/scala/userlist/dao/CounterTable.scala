package userlist.dao

import java.util.UUID

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import CounterTable._

object CounterTable {
  final case class CounterEntity(id: UUID, count: Long = 0L)
}

class CounterTable(tag: Tag) extends Table[CounterEntity](tag, "counters") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def count: Rep[Long] = column[Long]("count", O.Default(0L))

  override def * : ProvenShape[CounterEntity] =
    (id, count).mapTo[CounterEntity]
}
