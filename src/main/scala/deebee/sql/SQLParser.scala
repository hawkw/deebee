package deebee.sql

import deebee.sql.ast._

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

  override val lexical = new SQLLexical
  type NumericParser[T] = String => T

  lexical.reserved ++= List("select", "as", "or", "and", "group", "order", "by", "where", "limit",
    "join", "asc", "desc", "from", "on", "not", "having", "distinct",
    "case", "when", "then", "else", "end", "for", "from", "exists", "between", "like", "in",
    "year", "month", "day", "null", "is", "date", "interval", "group", "order",
    "date", "left", "right", "outer", "inner")

  lexical.delimiters ++= List(
    "*", "+", "-", "<", "=", "<>", "!=", "<=", ">=", ">", "/", "(", ")", ",", ".", ";"
    )

  // parser for ints
  protected var intParser : NumericParser[Int] = {_.toInt}

  def createTable: Parser[Schema] = ("create" ~ "table") ~> name ~ "(" ~ repsep(attr, ",") <~ ")" ^^{
    case n ~ "(" ~ attrs => new Schema(n, attrs)
  }
  def attr: Parser[Column] = name ~ typ ~ constraint.* ^^{ case n ~ dt ~ cs => Column(n, dt, cs) }
  def typ: Parser[Type] = (
    ("int" | "integer") ^^^ Integer
      | "char" ~> "(" ~> int <~ ")" ^^{ case i => Char(i) }
      | "varchar" ~> "(" ~> int <~ ")" ^^{ case n => Varchar(n) }
      | "numeric" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Numeric(p,d)}
      | "decimal" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Decimal(p,d)}
    )
  def int = accept("number", { case lexical.NumericLit(n) => intParser.apply(n)} )
  def name  = accept("string", { case lexical.StringLit(n) => n} )
  def constraint: Parser[Constraint] = (
    ("not" ~ "null") ^^^ Not_Null
      | ("primary" ~ "key") ^^^ Primary_Key
      | ("foreign" ~ "key")  ^^^ Foreign_Key
      | "unique" ^^^ Unique
    )

}
