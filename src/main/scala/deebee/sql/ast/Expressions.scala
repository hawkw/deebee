package deebee
package sql.ast
import scala.reflect.runtime.universe._

/**
 * Created by hawk on 11/23/14.
 */
sealed trait Expr[T] extends Node {
  val typeof = weakTypeTag[T]
}
case class Const[T](x: T) extends Expr[T] {
  override val emitSQL = x match {
    case s: String => "\'" + x.toString + "\'"
    case _ => x.toString
  }
}
case class Ident(name: String) extends Expr[String] {
  override val emitSQL = name
}
case class Comparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Boolean] {
  override lazy val emitSQL = s"$left $op $right"
  def emitPredicate(context: Relation) = ??? // TODO: this would generate the actual predicate to be used by the query executor
}
case class ParenComparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Boolean] {
  def this(c: Comparison) = this(c.left, c.op, c.right)
  override lazy val emitSQL = s"($left $op $right)"
}