package deebee
package sql
package ast

import deebee.storage.Entry
/**
 * AST nodes for SQL statements
 *
 * Created by hawk on 11/21/14.
 */
sealed trait Stmt extends Node

case class SelectStmt(
  projections: List[Proj] = GlobProj :: Nil,
  from: Ident,
  where: Option[Comparison] = None,
  //TODO: implement these
  //groupBy: Option[GroupBy] = None,
  //orderBy: Option[OrderBy] = None,
  limit: Option[Expr[Int]] = None
                   ) extends Stmt {

  override def emitSQL =
    s"SELECT ${projections.map(_.emitSQL).mkString(", ")}" +
    s" FROM ${from.emitSQL}"+
    s"${where.map(" WHERE " + _.emitSQL).getOrElse("")}"+
    s"${limit.map(" LIMIT " + _).getOrElse("")};"
    //groupBy.map(_.emitSQL),
    //orderBy.map(_.emitSQL),

}

case class DeleteStmt(
  from: Ident,
  where: Option[Comparison] = None,
  limit: Option[Expr[Int]] = None
  ) extends Stmt {

  override def emitSQL =
    s"DELETE FROM ${from.emitSQL}" +
      s"${where.map(" WHERE " + _.emitSQL).getOrElse("")}"+
      s"${limit.map(" LIMIT " + _).getOrElse("")};"
}

case class Attribute[T](
                   name: Ident,
                   datatype: Type[T],
                   constraints: List[Constraint]
                   ) extends Node {
  override def emitSQL = s"$name ${datatype.emitSQL}${constraints.map(" " + _.emitSQL).mkString}"
  def apply(context: Relation)(a: T): Entry[T] = {
    // TODO: apply {Unique | Not Null | Primary Key} constraints here
      datatype.entry(a)
  }
}

case class CreateStmt(
                   name: Ident,
                   attributes: List[Attribute[_]],
                   constraints: List[Constraint] = Nil
                   ) extends Node {
  override def emitSQL = {
    s"CREATE TABLE $name (\n\t${attributes.map(_.emitSQL).mkString(",\n\t")}" +
      {if (constraints.length > 0) ",\n\t" else ""} +
      s"${constraints.map(_.emitSQL).mkString("\n\t")}\n);"
  }
}

case class DropStmt(name: Ident) extends Node {
  override def emitSQL = s"DROP TABLE $name;"
}

case class InsertStmt(into: Ident, values: List[Expr[_]]) extends Node {
  override def emitSQL = s"INSERT INTO $into VALUES (${values.map(_.toString).mkString(", ")});"
}
