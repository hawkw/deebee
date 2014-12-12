package deebee
package frontends

import java.io.PrintStream
import java.nio.file.{Paths, Files}

import scala.util.{Success,Failure}

import deebee.sql.SQLParser


case class Config(
                   path: String = System.getProperty("user.dir"),
                   verbose: Boolean = false,
                   demo: Boolean = false,
                   parserDemo:Boolean = false
                   )

/**
 * DeeBee interactive shell. Loosely inspired by the SQLite3 one.
 * @author Hawk
 * Created by hawk on 12/10/14.
 */
object Shell {
  protected val parser  = new scopt.OptionParser[Config]("deebee-shell") {
    head("deebee-shell","0.1")
    arg[String]("directory") optional() action { (x, c) =>
    c.copy(path = x) } validate { x =>
      if ( Files.isWritable(Paths get x) && Files.isReadable(Paths get x)) success
      else failure(s"Path $x was not readable or writable")
    } text "directory containing the database to connect to"
    opt[Unit]('v', "verbose") action{ (_, c) =>
    c.copy(verbose = true)} text "enable verbose logging mode"
    opt[Unit]('d', "demo") action{ (_, c) =>
      c.copy(demo = true)} text "run the faculty database demo"
    opt[Unit]('p', "parse") action{ (_, c) =>
      c.copy(parserDemo = true)} text "run the parser  demo"
    help("help") text "prints this usage text"
  }

  /**
   * Runs the shell over a given connection.
   * @param conn a connection to the database to provide a shellf or
   * @param in Input stream. Default is stdin.
   * @param out Output stream for general results. Default is stdout.
   * @param err Output stream for errors. Default is stderr.
   */
  def run(
           conn: Connection,
           in: io.BufferedSource = io.Source.stdin,
           out: PrintStream = System.out,
           err: PrintStream = System.err
           ): Unit = {

    val name = conn.name
    out.println("Welcome to the DeeBee Interactive Demo!\n" +
      "Enter SQL commands at the prompt. Type `.exit` to exit.")
    out.print(s"$name> ")
    for {line <- in.getLines()} {
      line match {
        case ".exit" => System.exit(0)
          // TODO: provide something like SQLite's ".tables" directive
        case _ =>
          conn.statement(line) match {
            case Success(Some(res)) => out.println(s"$name>>> $res")
            case Success(None) => {}
            case Failure(why) => err.println(s"$name>>> $why")
          }
          out.print(s"$name> ")
      }
    }
  }

  /**
   * Runs the DeeBee shell, dispatching to the parser
   * and demo shells or to the main shell, as needed.
   * @param args any command-line arguments
   */
  def main(args: Array[String]): Unit = parser.parse(args, Config()) map { _ match {
      case Config(_,v,false,true) => SQLParser.main(args)
      case Config(_,v,true,false) => DemoShell.main(args)
      case Config(path,v,false,false) => run(
        path match {
          case p: String if p == System.getProperty("user.dir") =>
            Connection("deebee", p)
          case p: String =>
            val np = p.split("/")
            Connection(
              np.last,
              np.dropRight(1).mkString
            )
        }
      )
    }
  } getOrElse {
    System.exit(1)
  }

}
