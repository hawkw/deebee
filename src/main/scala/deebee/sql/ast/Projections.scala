package deebee.sql.ast

/**
 * Created by hawk on 11/21/14.
 */
sealed trait Proj extends Node
case class ExprProj(expr: Expr[String], as: Option[String]) extends Proj {
  override def emitSQL = List(Some(expr.emitSQL), as).flatten.mkString(" as ")
}
case object GlobProj extends Proj {
  override def emitSQL = "*"
}