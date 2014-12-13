package deebee
package storage

import akka.actor.{Actor, ActorLogging}
import deebee.exceptions.{InternalStateException, QueryException}
import deebee.sql.ast._

import scala.util.{Success, Try, Failure}

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.


/* */
abstract class RelationActor(
                         val name: String,
                         val attributes: List[Attribute],
                         val constraints: List[Constraint]
                         ) extends Actor with Relation with Selectable with Modifyable {

  override def receive: Receive = {
    case s: SelectStmt if s.table.name == this.name => sender ! process(s)
    case s: DeleteStmt if s.table.name == this.name => sender ! process(s)
    case s: InsertStmt if s.table.name == this.name => sender ! process(s)
    case s: DMLStmt if s.table.name != this.name => sender ! Failure(
      new InternalStateException(s"$name received a DML query intended for ${s.table.name}."))
    case a: Any => sender ! Failure(new InternalStateException(s"$name didn't know how to handle $a."))
  }
}
*/

/**
 * Process `SELECT`, `INSERT`, and `DELETE` statements when sent by the supervising [[Database]].
 * This will also handle `UPDATE` statements when they are implemented.
 *
 * The supervising database will send these messages as [[Stmt]]s from the [[deebee.sql.ast]] package when it
 * receives them from the frontend (either an API connection or a SQL console). The database is responsible for
 * dispatching these queries to the appropriate child table's actor. Query results (if any) will be sent back to
 * the sender.
 *
 * These queries should be able to be handled at this level of abstraction using the database's internal API
 * ([[Relation.project()]], [[Relation.filter()]], [[Relation.rows( )]]) and filtering them by the received
 * [[Stmt]]'s predicates (if any). This means that the concrete [[Relation]] implementations need not deal with
 * query processing, and must only provide the internal API for providing columns and rows.
 */
trait RelationActor {
  def select(statement: SelectStmt): Try[Relation]
  def delete(statement: DeleteStmt): Unit
  def insert(statement: InsertStmt): Unit
}