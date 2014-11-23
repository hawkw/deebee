package deebee.sql.ast

/**
 * AST nodes for SQL statements
 *
 * TODO: add INSERT
 * TODO: add DELETE
 *
 * Created by hawk on 11/21/14.
 */
sealed trait Stmt extends Node
case class Select(
  projections: List[Proj] = GlobProj :: Nil,
  from: List[Const[String]],
  where: Option[Predicate] = None,
  //TODO: implement these
  //groupBy: Option[GroupBy] = None,
  //orderBy: Option[OrderBy] = None,
  limit: Option[Int] = None
                   ) extends Stmt {

  override def emitSQL =
    s"SELECT  ${projections.map(_.emitSQL).mkString(", ")}" +
    s" FROM ${from.map(_.emitSQL).mkString(",")} "+
    s"${where.map("WHERE" + _.emitSQL)}${limit.map(" LIMIT " + _)};"
    //groupBy.map(_.emitSQL),
    //orderBy.map(_.emitSQL),

}
case class Column(
                   name: Expr[String],
                   datatype: Type,
                   constraints: List[Constraint]
                   ) extends Node {
  override def emitSQL = s"$name ${datatype.emitSQL}${constraints.map(" " + _.emitSQL).mkString}"
}

case class CreateStmt(
                   name: Expr[String],
                   attributes: List[Column],
                   constraints: List[Constraint] = Nil
                   ) extends Node {
  override def emitSQL = {
    s"CREATE TABLE $name (\n\t${attributes.map(_.emitSQL).mkString(",\n\t")}" +
      {if (constraints.length > 0) ",\n\t" else ""} +
      s"${constraints.map(_.emitSQL).mkString("\n\t")}\n);"
  }
}
