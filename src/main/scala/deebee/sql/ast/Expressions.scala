package deebee
package sql.ast

import deebee.exceptions.QueryException
import deebee.Relation

import scala.util.{Failure, Success, Try}

/**
 * Trait for SQL exceptions.
 *
 * Created by hawk on 11/23/14.
 */
sealed trait Expr[T] extends Node {
  /**
   * Evaluate this expression against the given context, emitting a [[Try]] containing either
   * whatever this expression produces, or any semantic errors generated while processing the expression.
   *
   * This needs to be contextualized in order to access the schema and state of the table the expression is
   * evaluated relative to.
   *
   * Query processing exceptions include type errors, accesses to nonexistent attributes, or any malformed
   * syntax that might slip through the parser. These are generally instances of [[QueryException]] - handle
   * these with warnings. If a different type of exception ever slips through, that probably is fatal and should
   * be thrown to top level.
   *
   * @param context The relation against which to evaluate the expression.
   * @return a [[Try]] containing either whatever the expression generates ([[T]]) or any [[Exception]]s
   *         generated during processing.
   */
  def emit(context: Relation): Try[T] = ???
}
case class Const[T](x: T) extends Expr[T] {
  override val emitSQL = x match {
    case s: String => "\'" + x.toString + "\'"
    case _ => x.toString
  }
  override def emit(context: Relation) = Success(x)
}
case class Ident(name: String) extends Expr[Int] {
  override val emitSQL = name
  override def emit(context: Relation): Try[Int] = context.attributes.indexWhere(_.name == name) match {
    case -1 => Failure(new QueryException(s"Relation $context did not contain attribute $name"))
    case i: Int => Success(i)
  }
}

/**
 * Representation for a comparison.
 *
 * [[Comparison# e m i t ( ) e m i t]]s a partial function taking [[Row]]s and returning
 * true or false. This can then be used for filtering the database rows by the predicate,
 * such as when processing a SELECT statement.
 *
 * @param left the left-hand side of the predicate. Currently this is either a row name or another predicate
 * @param op the operator
 * @param right the right-hand side of the predicate
 */
case class Comparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Row => Boolean] {

  override lazy val emitSQL = s"$left $op $right"

  implicit def toPredicate(pred: Row => Boolean): Predicate = new Predicate(pred)

  /**
   * Attempt to process the comparison represented by this AST node against the specified context.
   * @param context The relation against which to evaluate the expression.
   * @return a [[Try]] containing either whatever the expression generates
   *         ([[Row]] => [[Boolean]]) or any [[Exception]]s generated during processing.
   */
  override def emit(context: Relation): Try[Row => Boolean] = (for {
    leftside <- left.emit(context)
    rightside <- right.emit(context)
  } yield {
    (leftside, op, rightside) match {
      case (l: (Row => Boolean), "AND", r: (Row => Boolean)) => Success(l && r)
      case (l: (Row => Boolean), "OR", r: (Row => Boolean)) => Success(l || r)
      case (l: Int, "=" | "==", Const(value)) => Success({ x: Row =>
        x(l) == value
      })
      case (l: Int, "!=", Const(value)) => Success({ x: Row =>
        x(l) != value
      })
      case (l: Int, ">", Const(value)) => (context.attributes(l).datatype, value) match {
        case (IntegerType, v: Int) => Success({ x: Row => x(l).asInstanceOf[Int] > v})
        case (DecimalType(_,_), v: Double) => Success({ x: Row => x(l).asInstanceOf[Double] > v})
        case thing => Failure(new QueryException(s"TypeError: '>' requires numeric type, got $thing."))
      }
      case (l: Int, "<", Const(value)) => (context.attributes(l).datatype, value) match {
        case (IntegerType, v: Int) => Success({ x: Row => x(l).asInstanceOf[Int] < v})
        case (DecimalType(_,_), v: Double) => Success({ x: Row => x(l).asInstanceOf[Double] < v})
        case thing => Failure(new QueryException(s"TypeError: '<' requires numeric type, got $thing."))
      }
      case (l: Int, "<=", Const(value)) => (context.attributes(l).datatype, value) match {
        case (IntegerType, v: Int) => Success({ x: Row => x(l).asInstanceOf[Int] <= v})
        case (DecimalType(_,_), v: Double) => Success({ x: Row => x(l).asInstanceOf[Double] <= v})
        case thing => Failure(new QueryException(s"TypeError: '<=' requires numeric type, got $thing."))
      }
      case (l: Int, ">=", Const(value)) => (context.attributes(l).datatype, value) match {
        case (IntegerType, v: Int) => Success({ x: Row => x(l).asInstanceOf[Int] >= v})
        case (DecimalType(_,_), v: Double) => Success({ x: Row => x(l).asInstanceOf[Double] >= v})
        case thing => Failure(new QueryException(s"TypeError: '>=' requires numeric type, got $thing."))
      }
      case (l: Int, "LIKE", Const(value)) => (context.attributes(l).datatype, value) match {
        case (CharType(_) | VarcharType(_), v: String) => Success({ x: Row => x(l).asInstanceOf[String].matches(v)})
        case thing => Failure(new QueryException(s"TypeError: 'LIKE' requires character type, got $thing."))
      }
    }
  }).flatten

  /**
   * Wraps partial functions of the form [[Row]] => [[Boolean]] to allow composition
   * @param pred a partial function [[Row]] => [[Boolean]]
   */
  class Predicate(val pred: Row => Boolean) extends (Row => Boolean) {
    def apply(x: Row) = pred(x)

    def &&(that: Row => Boolean) = new Predicate(x => pred(x) && that(x))

    def ||(that: Row => Boolean) = new Predicate(x => pred(x) || that(x))

    def unary_! = new Predicate(x => !pred(x))
  }
}

/**
 * Wraps a parenthesized comparison. Basically, this just farms out to
 * [[Comparison]] for all the heavy-lifting.
 *
 * @param left the left-hand side of the predicate. Currently this is either a row name or another predicate
 * @param op the operator
 * @param right the right-hand side of the predicate
 */
case class ParenComparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Row => Boolean] {
  override lazy val emitSQL = s"($left $op $right)"

  def this(c: Comparison) = this(c.left, c.op, c.right)

  override def emit(context: Relation) = new Comparison(left, op, right).emit(context)
}