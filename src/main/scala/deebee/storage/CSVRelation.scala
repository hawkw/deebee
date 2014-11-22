package deebee.storage

import java.io.File

import akka.actor.Actor.Receive
import deebee.Relation

/**
 * Created by hawk on 11/19/14.
 */
class CSVRelation(name: String, private val back: File) extends Relation(name) {
  override type Row = this.type


}
