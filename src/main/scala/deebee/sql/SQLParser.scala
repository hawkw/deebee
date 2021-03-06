package deebee
package sql

import deebee.exceptions._
import deebee.sql.ast._

import scala.util.Try
import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StandardTokenParsers

/**
 * Parser for the Structured Query Language.
 *
 * The [[SQLParser#parse parse()]] method takes a String containing a SQL query and outputs an [[scala.util.Try]]
 * on a [[ast.Node]] representing the abstract syntax tree generated by that source code.
 * The top-level node in a SQL AST is the type of statement corresponding to that query - for our purposes, this means
 * either one of the two supported DDL statements ([[ast.CreateStmt CREATE TABLE]] or [[ast.DropStmt DROP TABLE]]) or
 * one of the three supported DML statements ([[ast.SelectStmt SELECT]], [[ast.InsertStmt INSERT]], or [[ast.DeleteStmt DELETE]]).
 *
 * ==Note on Parsing==
 *
 * For anyone not familiar with Scala's [[scala.util.parsing.combinator parser-combinators]] library,
 * I strongly suggest you check it out.
 *
 * Parser combinators represent a functional programming approach to text parsing that allow developers
 * to write recursive-descent parsers that are easy to write, human-readable, and modular, making them
 * very testable and maintainable.
 *
 * Essentially, the way it works is pretty simple: a parser combinator is a higher-order function
 * that takes in two parser functions (i.e. functions that accepts some strings and rejects others)
 * and returns a new parser function that combines the two input parsers in a specified fashion.
 * This allows a programmer to construct a complete parser for some grammar out of smaller pieces.
 *
 * Due to Scala's function application semantics, its' parser combinators library essentially forms
 * an embedded DSL that allows parsers to be specified in a syntax that closely resembles BNF. This
 * means that writing a parser in Scala is a lot like writing a parser in Bison (or other parser
 * generators), but instead of writing a grammar description that is used to generate
 * (frequently messy, unreadable, and unmaintainable) source code, you are actually writing the source
 * for the parser. This is yet another example of the power of Scala and its' approach to programming.
 *
 * If you're interested to learn more about Scala parsing, I strongly suggest you read the paper
 * [[https://lirias.kuleuven.be/bitstream/123456789/164870/1/CW491.pdf "Parser Combinators in Scala"]]
 * by Odersky, Moors, and Piessens, and [[http://www.artima.com/pins1ed/combinator-parsing.html Chapter 31]]
 * in the book "Programming in Scala" by Martin Odersky, Lex Spoon, and Bill Venners, the first edition of which
 * is available for free online.
 *
 * ===PackratParsers===
 *
 * In the Deebee SQL parser, I make frequent use of [[scala.util.parsing.combinator.PackratParsers PackratParsers]].
 * These parsers function similarly to the `Parser` from [[scala.util.parsing.combinator.Parsers Parsers]], but
 * with the addition of a memoization facility. This allows us to implement back-tracking, recursive-descent parsers,
 * but with the added guarantees of unlimited lookahead and linear parse time. `PackratParsers` also allows us to accept
 * left-recursive grammars without infinite recursion. There isn't a whole lot of left recursion in the SQL grammar
 * I'm attempting to implement in Deebee, but the performance improvement is always nice.
 *
 * If you're interested in packrat parsing, this technique was first described in a very interesting paper which can be
 * found [[http://scala-programming-language.1934581.n4.nabble.com/attachment/1956909/0/packrat_parsers.pdf here]].
 *
 * @author Hawk Weisman
 */

object SQLParser extends StandardTokenParsers with PackratParsers {

  class SQLLexical extends StdLexical with PackratParsers {

    import scala.util.parsing.input.CharArrayReader.EofCh

    override def whitespace: Parser[Any] = rep(
      whitespaceChar
        | '/' ~ '*' ~ comment
        | '#' ~ rep(chrExcept(EofCh, '\n'))
        | '-' ~ '-' ~ rep(chrExcept(EofCh, '\n'))
        | '/' ~ '*' ~ failure("unclosed comment")
    )

    override protected def processIdent(name: String) =
      if (reserved contains name.toLowerCase) Keyword(name.toLowerCase) else Identifier(name)

    override protected def comment: Parser[Any] = (
      '*' ~ '/' ^^ { case _ => ' '}
        | chrExcept(EofCh) ~ comment
      )

  }

  type NumericParser[T] = String => T
  type P[T] = PackratParser[T]
  override val lexical = new SQLLexical

  lexical.reserved ++= List("create", "drop", "table", "int", "integer", "char", "varchar", "numeric",
    "decimal", "not", "null", "foreign", "primary", "key", "unique", "references", "select", "from", "as", "where",
    "and", "or", "limit", "delete", "insert", "into", "values"
  )

  lexical.delimiters ++= List(
    "*", "+", "-", "<", "=", "<>", "!=", "<=", ">=", ">", "/", "(", ")", ",", ".", ";"
  )

  // parser for ints
  protected val intParser: NumericParser[Int] = {
    _.toInt
  }
  protected val doubleParser: NumericParser[Double] = {
    _.toDouble
  }
  def int = accept("integer", { case lexical.NumericLit(n) => intParser.apply(n)})

  def double = numericLit ~ "." ~ numericLit ^^{ case first ~ "." ~ last => doubleParser.apply(first + "." +last)}

  def string = accept("string", { case lexical.StringLit(n) => n})


  lazy val query: P[Node] = (
    createTable
      | select
      | delete
      | insert
      | dropTable
    ) <~ ";"
  lazy val createTable: P[CreateStmt] = ("create" ~ "table") ~> identifier ~ "(" ~ rep1sep(attr | refConstraint, ",") <~ ")" ^^ {
    case name ~ "(" ~ contents => new CreateStmt(
      name,
      contents.filter {
        _.isInstanceOf[Attribute]
      }.asInstanceOf[List[Attribute]],
      contents.filter {
        _.isInstanceOf[Constraint]
      }.asInstanceOf[List[Constraint]]
    )
  }
  lazy val insert: P[InsertStmt] = "insert" ~> "into" ~> identifier ~ ("values" ~> "(" ~> repsep(literal, ",") <~ ")") ^^ {
    case into ~ values => InsertStmt(into, values)
  }

  lazy val dropTable: P[DropStmt] = "drop" ~> "table" ~> identifier ^^{case i => DropStmt(i)}
  lazy val attr: P[Attribute] = ident ~ typ ~ inPlaceConstraint.* ^^ { case name ~ dt ~ cs => Attribute(name, dt, cs)}
  lazy val typ: P[Type] = (
    ("int" | "integer") ^^^ IntegerType
      | "char" ~> "(" ~> int <~ ")" ^^{ case i => CharType(i) }
      | "varchar" ~> "(" ~> int <~ ")" ^^{ case n => VarcharType(n) }
      | ("numeric" | "decimal") ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => DecimalType(p,d)}
    )
  lazy val inPlaceConstraint: P[Constraint] = (
    ("not" ~ "null") ^^^ Not_Null
      | ("primary" ~ "key") ^^^ Primary_Key
      | "unique" ^^^ Unique
    )
  lazy val identifier: P[Ident] = ident ^^{ i => Ident(i)}
  lazy val refConstraint: P[Constraint] =
    "foreign" ~ "key" ~ "(" ~> rep1sep(identifier, ",") ~ ")" ~ "references" ~ opt(identifier) ~ "(" ~ rep1sep(identifier, ",") <~ ")" ^^{
      case cols ~ ")" ~ "references" ~ ref ~ "(" ~ othercols  =>
        Foreign_Key(cols,ref,othercols)
    }
  lazy val select: P[SelectStmt] = "select" ~> projections ~ "from" ~ identifier ~ opt(whereClause) ~ opt(limitClause) ^^{
    case proj ~ "from" ~ from ~ where ~ limit => SelectStmt(proj, from, where, limit)
  }
  lazy val delete: P[DeleteStmt] = "delete" ~> "from" ~> identifier ~ opt(whereClause) ~ opt(limitClause) ^^{
    case from ~ where ~ limit => DeleteStmt( from, where, limit )
  }
  lazy val projections: P[List[Proj]] = "*" ^^^ GlobProj :: Nil | rep1sep(exprProj, ",")
  lazy val exprProj: P[NameProj] = identifier ~ opt("as" ~> identifier) ^^{
    case proj ~ asPart => NameProj(proj, asPart)
  }
  lazy val whereClause: P[Comparison] = "where" ~> comparison
  lazy val limitClause: P[Expr[Int]] = "limit" ~> intExpr
  //lazy val parenComp: P[Comparison] = "(" ~> comparison <~ ")"
  lazy val comparison: P[Comparison] = (
    complexComparison
      | basicComparison
      |  "(" ~> comparison <~ ")"
    )
  lazy val complexComparison: P[Comparison]= (
    (comparison ~ "and" ~ comparison)
    | (comparison ~ "or" ~ comparison)
    ) ^^ {
    case lhs ~ op ~ rhs => Comparison(lhs, op.toUpperCase, rhs)
  }

  lazy val basicComparison: P[Comparison] = (
    (notCmpExpr ~ "=" ~ notCmpExpr)
    | (notCmpExpr ~ "!=" ~ notCmpExpr)
    | (notCmpExpr ~ "<>" ~ notCmpExpr)
    | (notCmpExpr ~ ">=" ~ notCmpExpr)
    | (notCmpExpr ~ "<=" ~ notCmpExpr)
    | (notCmpExpr ~ ">" ~ notCmpExpr)
    | (notCmpExpr ~ "<" ~ notCmpExpr)
    ) ^^{
    case lhs ~ op ~ rhs => Comparison(lhs, op.toUpperCase, rhs)
  }

  lazy val intExpr: P[Expr[Int]] = int ^^{ Const(_) }
    // | term // TODO: insert math here

  lazy val expression: P[Expr[_]] = comparison | notCmpExpr
  lazy val notCmpExpr: P[Expr[_]] = (
    literal
    | identifier ^^{ case i => i.asInstanceOf[Expr[_]] }
  )
  lazy val nullLit: P[Const[_]] = "null" ^^^ new NullConst
  lazy val literal: P[Const[_]] = (
    (
      int
        ||| double
        | stringLit
    ) ^^{ Const(_) }
      | nullLit
    )


  /**
   * Quick REPL for debugging. `.exit` exits.
   * @param args any command-line arguments passed
   */
  def main(args: Array[String]): Unit = {
    print("> ")
    for {line <- io.Source.stdin.getLines() if line != ".exit"}{
      println(">>> " + (parse(line) match {
          case util.Success(t) => t
          case util.Failure(e) => e
        })
      )
      print("> ")
    }
  }

  /**
   * Parse a SQL query into the corresponding AST
   * @param source A string containing the query
   * @return either a Success([[Node]]) with the root node of the AST
   *         for the query, or a Failure containing any
   *         [[QueryParsingException]]s that occurred.
   * @see [[sql.ast]]
   */
  def parse(source: String): Try[Node] = {
    phrase(query)(new lexical.Scanner(source)) match {
      case Success(result: Node, _) => util.Success(result)
      case x: Failure => util.Failure(new QueryParsingException(x.toString()))
      case x: Error => util.Failure(new QueryParsingException(x.toString()))
    }
  }
  /**
   * Parse a data literal into a corresponding constant.
   * This is used primarily for the CSV backend reading from persisted
   * files on disk.
   * @param lit A string containing the SQL literal
   * @return either a Success([[Const]]) with the root node of the AST
   *         for the query, or a Failure containing any
   *         [[QueryParsingException]]s that occurred.
   * @see [[sql.ast.Const]]
   * @see [[storage.CSVRelation.rows]]
   */
  def parseLit(lit: String): Try[Const[_]] = phrase(literal)(new lexical.Scanner(lit)) match {
    case Success(result: Const[_], _) => util.Success(result)
    case x: Failure => util.Failure(new QueryParsingException(x.toString()))
    case x: Error => util.Failure(new QueryParsingException(x.toString()))
  }


}
