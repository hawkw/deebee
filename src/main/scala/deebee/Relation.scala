package deebee

import akka.actor.Actor
import deebee.exceptions.InternalStateException
import deebee.sql.ast._

import scala.util.Failure

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.
 */
abstract class Relation(val name: String) extends Actor {
  type Row

  override def receive: Receive = {
    case SelectStmt(proj, table, where, limit) => if (table.name == this.name) {

    } else {
      sender ! Failure(new InternalStateException("Select was sent to wrong table"))
    }
    //case Insert(table, values) => ???
    //case Delete(table, where) => ???
  }
}
