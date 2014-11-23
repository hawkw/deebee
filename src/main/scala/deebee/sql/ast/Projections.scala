package deebee
package sql
package ast

/**
 * Created by hawk on 11/21/14.
 */
sealed trait Proj extends Node
case class NameProj(name: Ident, as: Option[Ident]) extends Proj {
  override def emitSQL = List(Some(name.emitSQL), as).flatten.mkString(" as ")
}
case object GlobProj extends Proj {
  override def emitSQL = "*"
}