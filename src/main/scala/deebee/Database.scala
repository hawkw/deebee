package deebee

import akka.actor.{TypedActor, Actor, ActorLogging, ActorSystem}
import deebee.sql.ast._
import deebee.storage.RelationActor

/**
 * Represents the top level of a database, responsible for sending queries
 * to the individual actors handling those tables and for managing the creation
 * of new tables.
 *
 * Eventually, this will also probably manage joins, when that happens.
 * @author Hawk Weisman
 */
abstract class Database(val name: String) extends Actor with ActorLogging {

  type Table <: RelationActor
  protected val system = ActorSystem("Database: " + name)
  private var tables = Map[String, Table]()

  override def receive: Receive = {
    case c: CreateStmt => if (tables contains c.name) {
      //TODO: eventually support the "IF NOT EXISTS" statement here
      log.warning(s"Could not create table ${c.name}, relation already exists")
    } else {
      tables += (c.name.toString -> create(c))
      log.info(s"Created table ${c.name}")
    }
    case DropStmt(which) => if (tables contains which) {
      TypedActor(system).stop(tables(which))
      tables -= which
      log.info(s"Dropped table $which")
    } else {
      log.warning(s"Could not drop table $which, no relation by that name exists")
    }
    case s: SelectStmt => sender ! (for (target <- tables get s.from; result <- target.select(s)) yield result)
    case i: InsertStmt => sender ! (for (target <- tables get i.into; result <- target.insert(i)) yield result)
    case d: DeleteStmt => sender ! (for (target <- tables get d.from; result <- target.delete(d)) yield result)
  }

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  protected def create(c: CreateStmt): Table
}

