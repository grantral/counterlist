package userlist.dao

import java.util.UUID
import scala.concurrent.Future

import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import userlist.dao.CounterTable._

object CounterDao {
  def apply(db: Database): CounterDao =
    new CounterDao(db)
}

class CounterDao(db: Database) {
  val entities = TableQuery[CounterTable]

  private def saveAction(
      entity: CounterEntity
  ): FixedSqlAction[CounterEntity, NoStream, Effect.Write] =
    entities returning entities += entity

  private def filterById(id: UUID): Query[CounterTable, CounterEntity, Seq] =
    entities.filter(_.id === id)

  def save(entity: CounterEntity): Future[CounterEntity] =
    db.run {
      saveAction(entity).transactionally
    }

  def updateCount(entity: CounterEntity): Future[Int] =
    db.run {
      filterById(entity.id).map(_.count).update(entity.count).transactionally
    }

  def count(id: UUID): Future[Long] =
    db.run {
      filterById(id).map(_.count).result.head
    }

  def findOne(id: UUID): Future[Option[CounterEntity]] =
    db.run {
      filterById(id).result.headOption
    }

  def findAll: Future[Seq[CounterEntity]] =
    db.run {
      entities.result
    }
}
