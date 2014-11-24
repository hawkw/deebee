package deebee
package storage

import akka.actor.Actor
import deebee.exceptions.InternalStateException
import deebee.sql.ast._

import scala.util.Failure

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.
 */
abstract class Relation(
                         val name: String,
                         val attributes: List[Column[_]],
                         val constraints: List[Constraint]
                         ) extends Actor {

  /**
   * Return all entries from the named column
   * @param name the name of the column to select
   * @return A List of all the entries from the named column
   */
  def selectColumn(name: String): Row = selectColumn(attributes.indexWhere(_.name == name))

  /**
   * Return all entries from the `n`th column
   * @param n the number of the column to select
   * @return A List of all the entries from the `n`th colum
   */
  def selectColumn(n: Int): Row = ???

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  def selectRows: Set[List[Entry[_]]] = ???

  override def receive: Receive = {
    case SelectStmt(proj, table, where, limit) => if (table.name == this.name) {

    } else {
      sender ! Failure(new InternalStateException("Select was sent to wrong table"))
    }
    //case Insert(table, values) => ???
    //case Delete(table, where) => ???
  }
}
