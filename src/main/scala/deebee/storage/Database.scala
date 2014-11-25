package deebee
package storage

import akka.actor.{Actor, ActorSystem}
import com.typesafe.scalalogging.slf4j.LazyLogging
import deebee.sql.ast._

/**
 * Represents the top level of a database, responsible for sending queries
 * to the individual actors handling those tables and for managing the creation
 * of new tables.
 *
 * Eventually, this will also probably manage joins, when that happens.
 * @author Hawk Weisman
 */
abstract class Database(val name: String) extends Actor with LazyLogging {

  type Table <: Relation
  val system = ActorSystem("Database: " + name)
  var tables = Map[String, Table]()

  override def receive: Receive = {
    case c: CreateStmt => if (tables contains c.name) {
      logger.warn(s"Could not create table ${c.name}, relation already exists")
    } else {
      tables += (c.name.toString -> create(c))
      logger.info(s"Created table ${c.name}")
    }
    case DropStmt(which) => if (tables contains which) {
      tables -= which
      logger.info(s"Dropped table $which")
    } else {
      logger.warn(s"Could not drop table $which, no relation by that name exists")
    }
    case s: SelectStmt => sender ! (for (target <- tables get s.from) yield ())
  }

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  protected def create(c: CreateStmt): Table = ???
}

