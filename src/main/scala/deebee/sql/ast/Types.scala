package deebee
package sql
package ast

import java.util.Date
import deebee.storage.{DecimalEntry, VarcharEntry, CharEntry, Entry}

/**
 * AST nodes for data types
 *
 * Created by hawk on 11/21/14.
 */
sealed trait Type[T] extends Node {
  override def emitSQL = this
    .getClass
    .getSimpleName
    .replace("Type", "")
    .replace("$", "")
    .toUpperCase
  def entry(a: T): Entry[T] = ???
}
case object IntegerType extends Type[Int]

case class CharType(n: Const[Int]) extends Type[String] {
  override def emitSQL = s"CHAR($n)"
  override def entry(a: String) = new CharEntry(a, n)
}
case class VarcharType(n: Const[Int]) extends Type[String] {
  override def emitSQL = s"VARCHAR($n)"
  override def entry(a: String) = new VarcharEntry(a, n)
}
case class DecimalType(p: Const[Int], s: Const[Int]) extends Type[Double] {
  override def emitSQL = s"DECIMAL($p, $s)"
  override def entry(a: Double) = new DecimalEntry(a, p, s)
}
case object DateType extends Type[Date]