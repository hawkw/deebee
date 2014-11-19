package deebee

import akka.actor.Actor

/**
 * Definition for the table API.
 *
 * Created by hawk on 11/19/14.
 */
abstract class Table extends Actor {
  type Row
}
