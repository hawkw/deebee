package deebee

import akka.actor.Actor
import deebee.exceptions.InternalStateException
import deebee.query.{Delete, Insert, Select}

import scala.util.Failure

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.
 */
abstract class Table extends Actor {
  type Row

  override def receive: Receive = {
    case Select(table, rows, where) => if (table == this) {

    } else {
      sender ! Failure(new InternalStateException("Select was sent to wrong table"))
    }
    case Insert(table, values) => ???
    case Delete(table, where) => ???
  }
}
