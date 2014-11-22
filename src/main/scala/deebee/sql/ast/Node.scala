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
sealed trait Expr[T] extends Node
case class Const[T](x: T) extends Expr[T] {
  override def toString = x.toString
}

case class Column(
                   name: Expr[String],
                   datatype: Type,
                   constraints: List[Constraint]
                   ) extends Node {
  override def emitSQL = s"$name ${datatype.emitSQL} ${constraints.map(_.emitSQL).mkString(" ")}"
}

case class Schema(
  name: Expr[String],
  attributes: List[Column],
  constraints: List[Constraint] = Nil
  ) extends Node {
  override def emitSQL = {
    s"CREATE TABLE $name (\n\t${attributes.map(_.emitSQL).mkString("\n\t")}\n" +
      s"${constraints.map(_.emitSQL).mkString("\n\t")});"
  }
}
