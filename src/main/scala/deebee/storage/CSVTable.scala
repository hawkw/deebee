package deebee.storage

import java.io.File

import akka.actor.Actor.Receive
import deebee.Table
import deebee.query.{Select,Insert,Delete}

/**
 * Created by hawk on 11/19/14.
 */
class CSVTable(private val back: File) extends Table {
  override type Row = this.type


}
