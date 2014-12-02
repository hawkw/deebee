package deebee
package sql
package ast

import java.util.Date
import deebee.exceptions.QueryException
import deebee.storage._

/**
 * AST nodes for data types
 *
 * Created by hawk on 11/21/14.
 */
sealed trait Type extends Node {
  override def emitSQL = this
    .getClass
    .getSimpleName
    .replace("Type", "")
    .replace("$", "")
    .toUpperCase
  def entry(a: Any): Entry[_]
}
case object IntegerType extends Type {
  override def entry(a: Any) = a match {
    case _: Int => new IntegerEntry(a.asInstanceOf[Int])
    case _ => throw new QueryException(s"TypeError when creating Integer entry")
  }
}

case class CharType(n: Const[Int]) extends Type {
  override def emitSQL = s"CHAR($n)"

  override def entry(a: Any) = a match {
    case _: String => new CharEntry(a.asInstanceOf[String], n)
    case _ => throw new QueryException(s"TypeError when creating Char entry")
  }
}
case class VarcharType(n: Const[Int]) extends Type {
  override def emitSQL = s"VARCHAR($n)"
  override def entry(a: Any) = a match {
    case _: String => new VarcharEntry(a.asInstanceOf[String], n)
    case _ => throw new QueryException(s"TypeError when creating Varchar entry")
  }
}
case class DecimalType(p: Const[Int], s: Const[Int]) extends Type {
  override def emitSQL = s"DECIMAL($p, $s)"
  override def entry(a: Any) = a match {
    case _: Double => new DecimalEntry(a.asInstanceOf[Double], p, s)
    case _ => throw new QueryException(s"TypeError when creating Decimal entry")
  }
}
case object DateType extends Type {
  override def entry(a: Any): Entry[_] = ???
}