package deebee
package sql
package ast

/**
 * Created by hawk on 11/21/14.
 */
sealed trait Proj extends Node {
  def emit: String
}
case class NameProj(name: Ident, as: Option[Ident]) extends Proj {
  override val emit: String = name

  override def emitSQL = List(Some(name.emitSQL), as).flatten.mkString(" as ")
}
case object GlobProj extends Proj {
  override val emit = "*"

  override def emitSQL = "*"
}