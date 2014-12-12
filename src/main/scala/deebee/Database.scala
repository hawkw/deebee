package deebee

import akka.actor.{TypedActor, ActorSystem}

import com.typesafe.scalalogging.slf4j.LazyLogging
import deebee.exceptions.QueryException
import deebee.frontends.Connection

import sql.ast._
import storage.RelationActor

import scala.util.{Try, Success, Failure}
import scala.collection.mutable

/**
 * Represents the top level of a database, responsible for sending queries
 * to the individual actors handling those tables and for managing the creation
 * of new tables.
 *
 * Eventually, this will also probably manage joins, when that happens.
 * @author Hawk Weisman
 */
abstract class Database(val name: String) extends LazyLogging {

  type Table <: RelationActor
  protected val system = ActorSystem("Database-" + name)
  protected val tables = mutable.Map[String, Table]()


  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  protected def create(c: CreateStmt): Table

  def connectTo = Connection.connect(this)
  def query(stmt: Node): Try[Option[Relation]] = stmt match {
      case c: CreateStmt => if (tables contains c.name) {
        //TODO: eventually support the "IF NOT EXISTS" statement here
        logger.warn(s"Could not create table ${c.name}, relation already exists")
        Success(None)
      } else {
        tables += (c.name.toString -> create(c))
        logger.info(s"Created table ${c.name}")
        Success(None)
      }
      case DropStmt(which) => tables get which match {
        case Some(table) =>
          TypedActor(system).stop(table)
          tables -= which
          logger.info(s"Dropped table $which")
          Success(None)
        case None =>
          logger.warn(s"Could not drop table $which, no relation by that name exists")
          Success(None)
      }
      case s: SelectStmt => tables get s.from match {
        case Some(table) => table.select(s).map(Some(_))
        case None => logger.warn(s"Could not select from ${s.from}, no relation by that name exists")
          Failure(new QueryException(s"Could not select from ${s.from}, no relation by that name exists"))
      }
      case d: DeleteStmt => tables get d.from match {
        case Some(table) => Try(table.delete(d)).map(_ => None)
        case None => logger.warn(s"Could not delete from ${d.from}, no relation by that name exists")
          Failure(new QueryException(s"Could not delete from ${d.from}, no relation by that name exists"))
      }
      case i: InsertStmt => tables get i.into match {
        case Some(table) => Try(table.insert(i)) match {
          case Failure(why) => Failure(why)
          case Success(_) => Success(None)
        }
        case None => logger.warn(s"Could not insert into from ${i.into}, no relation by that name exists")
          Failure(new QueryException(s"Could not insert into from ${i.into}, no relation by that name exists"))
      }
    }
}

