package deebee.sql

import deebee.exceptions.QueryParsingException
import deebee.sql.ast._

import scala.util.Try
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StandardTokenParsers

/**
 * Parser for the Structured Query Language
 * Created by hawk on 11/20/14.
 */

object SQLParser extends StandardTokenParsers  {

  class SQLLexical extends StdLexical {
    override protected def processIdent(name: String) =
      if (reserved contains name.toLowerCase) Keyword(name.toLowerCase) else Identifier(name)
  }

  def parse(source: String): Try[Node] = {
    val scan = new lexical.Scanner(source)
    phrase(query)(scan) match {
      case Success(result: Node, _) => util.Success(result)
      case x: Failure => util.Failure(new QueryParsingException(x.toString()))
      case x: Error => util.Failure(new QueryParsingException(x.toString()))
    }
  }

  override val lexical = new SQLLexical
  type NumericParser[T] = String => T

  lexical.reserved ++= List("create", "table", "int", "integer", "varchar", "numeric",
    "decimal", "not", "null", "foreign", "primary", "key", "unique")

  lexical.delimiters ++= List(
    "*", "+", "-", "<", "=", "<>", "!=", "<=", ">=", ">", "/", "(", ")", ",", ".", ";"
  )

  // parser for ints
  protected var intParser : NumericParser[Int] = {_.toInt}
  def query: Parser[Node] = createTable
  //  | select // todo: implement

  def createTable: Parser[Schema] = ("create" ~ "table") ~> ident ~ "(" ~ repsep(attr, ",") <~ ")" <~ ";" ^^{
    case name ~ "(" ~ attrs => new Schema(name, attrs)
  }
  def attr: Parser[Column] = ident ~ typ ~ constraint.* ^^{ case name ~ dt ~ cs => Column(name, dt, cs) }
  def typ: Parser[Type] = (
    ("int" | "integer") ^^^ Integer
      | "char" ~> "(" ~> int <~ ")" ^^{ case i => Char(i) }
      | "varchar" ~> "(" ~> int <~ ")" ^^{ case n => Varchar(n) }
      | "numeric" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Numeric(p,d)}
      | "decimal" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Decimal(p,d)}
    )
  def int = accept("number", { case lexical.NumericLit(n) => intParser.apply(n)} )
  def string  = accept("string", { case lexical.StringLit(n) => n} )
  def constraint: Parser[Constraint] = (
    ("not" ~ "null") ^^^ Not_Null
      | ("primary" ~ "key") ^^^ Primary_Key
      | ("foreign" ~ "key")  ^^^ Foreign_Key
      | "unique" ^^^ Unique
    )

}
