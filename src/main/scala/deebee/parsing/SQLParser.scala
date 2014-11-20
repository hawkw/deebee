package deebee.parsing

import deebee.schema._

import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StandardTokenParsers

/**
 * Created by hawk on 11/20/14.
 */

object SQLParser extends StandardTokenParsers  {

  class SQLLexical extends StdLexical {
    override protected def processIdent(name: String) =
      if (reserved contains name.toLowerCase) Keyword(name.toLowerCase) else Identifier(name)
  }

  lexical.reserved ++= List("select", "as", "or", "and", "group", "order", "by", "where", "limit",
    "join", "asc", "desc", "from", "on", "not", "having", "distinct",
    "case", "when", "then", "else", "end", "for", "from", "exists", "between", "like", "in",
    "year", "month", "day", "null", "is", "date", "interval", "group", "order",
    "date", "left", "right", "outer", "inner")

  lexical.delimiters ++= List("(", ")", ";", ",")


  /** Type signature for functions that can parse numeric literals */
  type NumericParser[T] = String => T

  // parser for ints
  protected var intParser : NumericParser[Int] = {_.toInt}

  def createTable: Parser[Schema] = ("create" ~ "table") ~> name ~ "(" ~ repsep(attr, ",") <~ ")" ^^{
    case n ~ "(" ~ attrs => new Schema(n, attrs.toMap)
  }
  def attr: Parser[(String,Column)] = name ~ typ ~ constraint.* ^^{ case n ~ dt ~ cs => n -> Column(dt, cs) }
  def typ: Parser[Datatype] = (
    ("int" | "integer") ^^^ Integer
      | "char" ~> "(" ~> int <~ ")" ^^{ case i => Char(i) }
      | "varchar" ~> "(" ~> int <~ ")" ^^{ case n => Varchar(n) }
      | "numeric" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Numeric(p,d)}
      | "decimal" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Decimal(p,d)}
    )
  def int = accept("number", { case lexical.NumericLit(n) => intParser.apply(n)} )
  def name  = accept("string", { case lexical.StringLit(n) => n} )
  def constraint: Parser[Constraint] = (
    ("not" ~ "null") ^^^ NotNull
      | ("primary" ~ "key") ^^^ PrimaryKey
      | ("foreign" ~ "key")  ^^^ ForeignKey
      | "unique" ^^^ Unique
    )

}
