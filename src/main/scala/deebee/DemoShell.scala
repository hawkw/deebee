package deebee

import deebee.exceptions.QueryException
import deebee.{Relation, View, Row}
import deebee.storage._
import deebee.sql.ast._
import deebee.sql.SQLParser

import scala.util.{Failure, Success}

/**
 * Created by hawk on 12/2/14.
 */
object DemoShell {

  val faculty = new View(
    Set[Row](
      Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
      Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
      Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
      Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
    ),
    Seq[Attribute[_]](
      Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
      Attribute("first_name", VarcharType(25), Nil),
      Attribute("last_name", VarcharType(25), Nil),
      Attribute("office", VarcharType(25), Nil)
    )
  )

  /**
   * Quick REPL for debugging. `.exit` exits.
   * @param args
   */
  def main(args: Array[String]): Unit = {
    var line = ""
    while(line != ".exit") {
      print("> ")
      line = Console.in.readLine()
      println((SQLParser.parse(line) match {
        case util.Success(t) => t match {
          case SelectStmt(projections, table, where, limit) => println(
            if (table.name == "faculty") {
              val predicate = where
                .map(clause => clause.emit(faculty))
              (projections match {
                case GlobProj :: Nil => Success(if (predicate.isDefined) {
                  faculty.filter(predicate.get.get)
                } else faculty)
                case Nil => Failure(new QueryException("Received a SELECT statement with no projections."))
                case p: Seq[Proj] if p.length > 0 => Success((if (predicate.isDefined) {
                  faculty.filter(predicate.get.get)
                } else faculty).project(p.map(_.emit)))
              }).map(results =>
                results.take(
                  limit
                    .map(_
                    .emit(faculty)
                    .get // this will be Success because it's a constant.
                    )
                    .getOrElse(results.rows.size)
                  )
                )
            }.getOrElse("")
          )
          case _ => t
        }
        case util.Failure(e) => e
      })
      )
    }
  }

}
