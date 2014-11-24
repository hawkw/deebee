package deebee
package sql
package ast
/**
 * AST nodes for data types
 *
 * Created by hawk on 11/21/14.
 */
sealed trait Type extends Node {
  override def emitSQL = this
    .getClass
    .getSimpleName
    .toUpperCase
    .replace("$", "")
}
case object Integer extends Type

case class Char(n: Expr[Int]) extends Type {
  override def emitSQL = s"CHAR($n)"
}
case class Varchar(n: Expr[Int]) extends Type {
  override def emitSQL = s"VARCHAR($n)"
}
case class Decimal(p: Expr[Int], s: Expr[Int]) extends Type {
  override def emitSQL = s"DECIMAL($p, $s)"
}
case object Date extends Type