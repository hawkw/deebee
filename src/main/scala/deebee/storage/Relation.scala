package deebee
package storage

import akka.actor.{Actor, ActorLogging}
import deebee.exceptions.{InternalStateException, QueryException}
import deebee.sql.ast._

import scala.util.Failure

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.
 */
abstract class Relation(
                         val name: String,
                         val attributes: List[Attribute[_]],
                         val constraints: List[Constraint]
                         ) extends Actor with ActorLogging {

  /**
   * Return all entries from the named column
   * @param name the name of the column to select
   * @return A List of all the entries from the named column
   */
  protected def column(name: String): Column = column(attributes.indexWhere(_.name == name))

  /**
   * Return all entries from the `n`th column
   * @param n the number of the column to select
   * @return A List of all the entries from the `n`th colum
   */
  protected def column(n: Int): Column

  /**
   * @return all columns from the table.
   */
  protected def columns: List[Column]

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  protected def rows: Set[Row]

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
   * ([[Relation.column( )]], [[Relation.columns( )]], [[Relation.rows( )]]) and filtering them by the received
   * [[Stmt]]'s predicates (if any). This means that the concrete [[Relation]] implementations need not deal with
   * query processing, and must only provide the internal API for providing columns and rows.
   * @return
   */
  override def receive: Receive = {
    case SelectStmt(projections, table, where, limit) => if (table.name == this.name) {
      sender ! (
        projections match {
          case GlobProj :: Nil => where match {
            case Some(clause) => for {pred <- clause.emit(this)} yield {
              rows.filter(pred)
            }
            case None => rows
          }
          case _ =>
            // this is crufty but should work for now
            // todo: refactor
            val cols = for {proj <- projections} yield {
              column(proj.emit).toIterator
            }
            var result: ResultSet = Stream[Row]()
            while (cols.forall(_.hasNext)) {
              result ++ cols.map(_.next)
            }
            sender ! result
          case Nil => Failure(new QueryException("Received a SELECT statement with no projections."))
        })

    } else {
      sender ! Failure(new InternalStateException("Select was sent to wrong table"))
    }
    //case Insert(table, values) => ???
    //case Delete(table, where) => ???
  }
}
