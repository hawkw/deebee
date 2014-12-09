package deebee.storage

import akka.actor.TypedProps
import akka.actor.TypedActor
import deebee.Database
import deebee.sql.ast._

/**
 * Created by hawk on 11/24/14.
 */
class CSVDatabase(name: String, val path: String) extends Database(name) {
  override type Table = CSVRelation with RelationActor

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  override protected def create(c: CreateStmt): Table =
    TypedActor(system)
      .typedActorOf(
        TypedProps(classOf[RelationActor],
          new CSVRelation(c, path)),
          c.name
      )
}
