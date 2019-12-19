package userlist

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.ShardRegion

object CounterHandler {
  final val ShardName = "counter"
  final val NumberOfShards = 10

  final case class EntityEnvelope(id: UUID, payload: Counter.Command)

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case EntityEnvelope(id, payload) => (id.toString, payload)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case EntityEnvelope(id, _) =>
      (math.abs(id.hashCode) % NumberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      (math.abs(id.hashCode) % NumberOfShards).toString
  }

  def props(counterRegion: ActorRef): Props =
    Props(new CounterHandler(counterRegion))
}

class CounterHandler(counterRegion: ActorRef) extends Actor {
  override def receive: Receive = {
    case cmd => counterRegion.forward(cmd)
  }
}
