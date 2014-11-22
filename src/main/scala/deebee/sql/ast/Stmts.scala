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