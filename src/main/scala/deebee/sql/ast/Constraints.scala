package deebee.sql.ast

/**
 * AST nodes for SQL constraints
 * Created by hawk on 11/21/14.
 */
sealed trait Constraint extends Node {
  override def emitSQL = this
    .getClass
    .getSimpleName
    .toUpperCase
    .replace("_", " ")
    .replace("$", "")
}
case object Primary_Key extends Constraint
case class Foreign_Key(
  cols: List[Ident],
  relation: Option[Ident] = None,
  references: List[Ident]) extends Constraint {
  override def emitSQL = s"FOREIGN KEY (${cols.map(_.emitSQL).mkString(",")}) " +
    s"REFERENCES ${relation.map(_.emitSQL).mkString} " +
    s"(${references.map(_.emitSQL).mkString(", ")})"
}
case object Not_Null extends Constraint
case object Unique extends Constraint