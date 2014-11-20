package deebee

import deebee.schema._

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

/**
 * Created by hawk on 11/20/14.
 */
object SQLParser extends StandardTokenParsers {
  lexical.reserved ++= List("create", "CREATE", "table", "TABLE", "values", "VALUES", "insert", "INSERT",
    "into", "INTO", "select", "SELECT", "from", "FROM", "delete", "DELETE", "where", "WHERE",
    "char", "CHAR", "varchar", "VARCHAR", "int", "INT", "integer", "INTEGER", "decimal", "DECIMAL",
    "NOT NULL", "not null", "PRIMARY KEY", "primary key", "UNIQUE", "unique", "FOREIGN KEY", "foreign key")

  lexical.delimiters ++= List("(", ")", ";", ",")

  /** Type signature for functions that can parse numeric literals */
  type NumericParser[T] = String => T

  // parser for ints
  protected var intParser : NumericParser[Int] = {_.toInt}

  def createTable: Parser[Schema] = (("CREATE" | "create") ~ ("table" | "TABLE")) ~> name ~ "(" ~ repsep(attr, ",") <~ ")" ^^{
    case n ~ "(" ~ attrs => new Schema(n, attrs.toMap)
  }
  def attr: Parser[(String,Column)] = name ~ typ ~ constraint.* ^^{ case n ~ dt ~ cs => n -> Column(dt, cs) }
  def typ: Parser[Datatype] = (
    ("int" | "integer" | "INT" | "INTEGER") ^^^ Integer
      | ("char" | "CHAR" ~ "(") ~> int <~ ")" ^^{ case i => Char(i) }
      | ("varchar" | "VARCHAR" ~ "(") ~> int <~ ")" ^^{ case n => Varchar(n) }
      | ("numeric" | "NUMERIC" ~ "(") ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Numeric(p,d)}
      | ("decimal" | "DECIMAL" ~ "(") ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Decimal(p,d)}
    )
  def int = accept("number", { case lexical.NumericLit(n) => intParser.apply(n)} )
  def name  = accept("string", { case lexical.StringLit(n) => n} )
  def constraint: Parser[Constraint] = (
    ("NOT NULL" | "not null") ^^^ NotNull
      | ("primary key" | "PRIMARY KEY") ^^^ PrimaryKey
      | ("foreign key" | "FOREIGN KEY") ^^^ ForeignKey
      | ("unique" | "UNIQUE") ^^^ Unique
    )

}
