package deebee.sql.ast

/**
 * Created by hawk on 11/21/14.
 */
trait Node {
  def emitSQL: String = ???
}
sealed trait Expr[T] extends Node
case class Const[T](x: T) extends Expr[T]

case class Column(
                   name: Expr[String],
                   datatype: Type,
                   constraints: List[Constraint]
                   ) extends Node {
  override def emitSQL = s"$name ${datatype.emitSQL} ${constraints.map(_.emitSQL).mkString}"
}

case class Schema(
  name: Expr[String],
  attributes: List[Column],
  constraints: List[Constraint] = Nil
  ) extends Node {
  override def emitSQL = {
    s"CREATE TABLE $name (\n${attributes.map(_.emitSQL).mkString("\n")}\n" +
      s"${constraints.map(_.emitSQL).mkString("\n")});"
  }
}
