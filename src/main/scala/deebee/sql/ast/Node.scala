package deebee.sql.ast

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
sealed trait Expr[T] extends Node
case class Const[T](x: T) extends Expr[T] {
  override val emitSQL = x.toString
}