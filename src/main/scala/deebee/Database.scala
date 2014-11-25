package deebee

import akka.actor.{Actor, ActorLogging, ActorSystem}
import deebee.sql.ast._
import deebee.storage.Relation

/**
 * Represents the top level of a database, responsible for sending queries
 * to the individual actors handling those tables and for managing the creation
 * of new tables.
 *
 * Eventually, this will also probably manage joins, when that happens.
 * @author Hawk Weisman
 */
abstract class Database(val name: String) extends Actor with ActorLogging {

  type Table <: Relation
  val system = ActorSystem("Database: " + name)
  var tables = Map[String, Table]()

  override def receive: Receive = {
    case c: CreateStmt => if (tables contains c.name) {
      //TODO: eventually support the "IF NOT EXISTS" statement here
      log.warning(s"Could not create table ${c.name}, relation already exists")
    } else {
      tables += (c.name.toString -> create(c))
      log.info(s"Created table ${c.name}")
    }
    case DropStmt(which) => if (tables contains which) {
      tables -= which
      log.info(s"Dropped table $which")
    } else {
      log.warning(s"Could not drop table $which, no relation by that name exists")
    }
    case s: SelectStmt => sender ! (for (target <- tables get s.from) yield ())
  }

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  protected def create(c: CreateStmt): Table
}

