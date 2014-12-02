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
 */
abstract class RelationActor(
                         val name: String,
                         val attributes: List[Attribute],
                         val constraints: List[Constraint]
                         ) extends Actor with ActorLogging with Relation {


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
   * @return
   */
  override def receive: Receive = {
    case SelectStmt(projections, table, where, limit) => if (table.name == this.name) {
      sender ! (for {
          predicate <- where map (_.emit(this)) getOrElse Success({_: Row => true})
        } yield {
        (projections match {
            case GlobProj ::Nil => Success(filter(predicate))
            case Nil => Failure(new QueryException("Received a SELECT statement with no projections."))
            case p: Seq[Proj] if p.length > 0 => Success(filter(predicate).project(p.map(_.emit)))
          }).map(results =>
          results.take(
            limit
              .map(_
                .emit(this)
                .get // this will be Success because it's a constant.
              )
              .getOrElse(results.rows.size)
          ))
        })
    } else {
      sender ! Failure(new InternalStateException("Select was sent to wrong table"))
    }
    //case Insert(table, values) => ???
    //case Delete(table, where) => ???
  }
}
