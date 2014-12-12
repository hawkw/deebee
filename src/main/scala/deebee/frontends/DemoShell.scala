package deebee.frontends

import deebee._
import deebee.exceptions._
import deebee.sql.SQLParser
import deebee.sql.ast._
import deebee.storage._

import scala.util.{Failure, Success, Try}

/**
 * A quick shell for demoing DML queries when DDL is not implemented.
 * Created by hawk on 12/2/14.
 */
object DemoShell {

  var faculty: Relation with Selectable with Modifyable = new View(
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
  )

  protected[deebee] def doDemo(): Unit = {
    println("Welcome to the DeeBee Interactive Demo!\nEnter SQL commands at the prompt, or type `.exit` to exit.")
    print("> ")
    for {line <- io.Source.stdin.getLines() if line != ".exit"}{
      println(
        SQLParser.parse(line) match {
          case util.Success(t) => Try(
            t match {
              case s: SelectStmt if s.from.name == "faculty" => faculty.process(s).foreach(println _)
              case i: InsertStmt if i.into.name == "faculty" => faculty.process(i) match {
                case Success(r) => faculty = r
                case Failure(why) => println(why)
              }
              case d: DeleteStmt if d.from.name == "faculty" => faculty.process(d) match {
                case Success(r) => faculty = r
                case Failure(why) => println(why)
              }
              case _ => t
            }
          ) match {
            case Success(fully) => println(fully)
            case Failure(qe: QueryException) => println(qe.getMessage)
          }
          case util.Failure(e) => e
        })
      print("> ")
    }
    System.exit(0)
  }
  /**
   * Quick REPL for debugging. `.exit` exits.
   * @param args any command-line args passed to the REPL. Currently none are supported.
   */
  def main(args: Array[String]): Unit = {
    args match {
      case Array("--parse") => SQLParser.main(args)
      case _ => doDemo()
    }
  }

}
