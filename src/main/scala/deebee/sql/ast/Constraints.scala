package deebee.sql.ast

/**
 * Created by hawk on 11/21/14.
 */
sealed trait Constraint extends Node {
  override def emitSQL = this
    .getClass
    .getSimpleName
    .toUpperCase()
    .replace("_", " ")
    .replace("$", "")
}
case object Primary_Key extends Constraint
case object Foreign_Key extends Constraint
case object Not_Null extends Constraint
case object Unique extends Constraint