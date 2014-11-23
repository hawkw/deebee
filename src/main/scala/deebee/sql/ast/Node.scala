package deebee
package sql
package ast

/**
 * Trait for a node in a SQL abstract syntax tree.
 *
 * @author Hawk Weisman
 *
 * Created by hawk on 11/21/14.
 */
trait Node {
  /**
   * Re-emit the SQL statement(s) corresponding to this node
   * @return
   */
  def emitSQL: String = ???
  override def toString = emitSQL
}
case class Ident(name: String) extends Node {
  override val emitSQL = name
}
