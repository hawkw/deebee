package deebee.sql

import deebee.exceptions.QueryParsingException
import deebee.sql.ast._

import scala.Error
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
 * either one of the two supported DDL statements ([[ast.CreateStmt CREATE TABLE]] or [[ast.Drop DROP TABLE]]) or
 * one of the three supported DML statements ([[ast.Select SELECT]], [[ast.Insert INSERT]], or [[ast.Delete DELETE]]).
 *
 * TODO: DROP TABLE, SELECT, INSERT, and DELETE are all varying degrees of not yet implemented.
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

  class SQLLexical extends StdLexical {

    import scala.util.parsing.input.CharArrayReader.EofCh

    override protected def processIdent(name: String) =
      if (reserved contains name.toLowerCase) Keyword(name.toLowerCase) else Identifier(name)

    override def whitespace: Parser[Any] = rep(
      whitespaceChar
        | '/' ~ '*' ~ comment
        | '#' ~ rep( chrExcept(EofCh, '\n') )
        | '-' ~ '-' ~ rep( chrExcept(EofCh, '\n') )
        | '/' ~ '*' ~ failure("unclosed comment")
    )
    override protected def comment: Parser[Any] = (
      '*' ~ '/'  ^^ { case _ => ' '  }
        | chrExcept(EofCh) ~ comment
      )

  }
  override val lexical = new SQLLexical
  type NumericParser[T] = String => T
  type P[T] = PackratParser[T]

  def parse(source: String): Try[Node] = {
    phrase(query)(new lexical.Scanner(source)) match {
      case Success(result: Node, _) => util.Success(result)
      case x: Failure => util.Failure(new QueryParsingException(x.toString()))
      case x: Error => util.Failure(new QueryParsingException(x.toString()))
    }
  }

  lexical.reserved ++= List("create", "table", "int", "integer", "char", "varchar", "numeric",
    "decimal", "not", "null", "foreign", "primary", "key", "unique", "references")

  lexical.delimiters ++= List(
    "*", "+", "-", "<", "=", "<>", "!=", "<=", ">=", ">", "/", "(", ")", ",", ".", ";"
  )

  // parser for ints
  protected var intParser : NumericParser[Int] = {_.toInt}
  lazy val query: P[Node] = createTable
  //  | select // todo: implement

  lazy val createTable: P[CreateStmt] = ("create" ~ "table") ~> ident ~ "(" ~ rep1sep(attr | refConstraint, ",")  <~ ")" <~ ";" ^^{
    case name ~ "(" ~ contents => new CreateStmt(
      name,
      contents.flatMap{case c: Column => c :: Nil; case _ => Nil},
      contents.flatMap{case c: Constraint => c :: Nil; case _ => Nil}
    )
  }
  lazy val attr: P[Column] = ident ~ typ ~ inPlaceConstraint.* ^^{ case name ~ dt ~ cs => Column(name, dt, cs) }
  lazy val typ: P[Type] = (
    ("int" | "integer") ^^^ Integer
      | "char" ~> "(" ~> int <~ ")" ^^{ case i => Char(i) }
      | "varchar" ~> "(" ~> int <~ ")" ^^{ case n => Varchar(n) }
      | "numeric" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Numeric(p,d)}
      | "decimal" ~> "(" ~> int ~ "," ~ int <~ ")" ^^{ case p ~ "," ~ d => Decimal(p,d)}
    )
  def int = accept("number", { case lexical.NumericLit(n) => intParser.apply(n)} )
  def string  = accept("string", { case lexical.StringLit(n) => n} )
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

}
