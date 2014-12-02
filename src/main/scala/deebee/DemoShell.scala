package deebee

import deebee.storage._
import deebee.sql.ast._
import deebee.sql.SQLParser

import scala.util.{Failure, Success}

/**
 * A quick shell for demoing DML queries when DDL is not implemented.
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
    Seq[Attribute](
      Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
      Attribute("first_name", VarcharType(25), Nil),
      Attribute("last_name", VarcharType(25), Nil),
      Attribute("office", VarcharType(25), Nil)
    )
  ) with Selectable

  /**
   * Quick REPL for debugging. `.exit` exits.
   * @param args any command-line args passed to the REPL. Currently none are supported.
   */
  def main(args: Array[String]): Unit = {
    var line = ""
    while(line != ".exit") {
      print("> ")
      line = Console.in.readLine()
      println(
        SQLParser.parse(line) match {
        case util.Success(t) => t match {
          case s: SelectStmt if s.from.name == "faculty" => faculty.process(s).getOrElse("")
          case _ => t
        }
        case util.Failure(e) => e
      })
    }
  }

}
