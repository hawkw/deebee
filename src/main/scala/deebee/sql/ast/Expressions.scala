package deebee
package sql.ast

import deebee.exceptions.QueryException
import deebee.storage.{Relation, Row, Entry}

import scala.util.{Failure, Try, Success}

/**
 * Created by hawk on 11/23/14.
 */
sealed trait Expr[T] extends Node {
  def emit(context: Relation): Try[T] = ???
}
case class Const[T](x: T) extends Expr[T] {
  override val emitSQL = x match {
    case s: String => "\'" + x.toString + "\'"
    case _ => x.toString
  }
}
case class Ident(name: String) extends Expr[Int] {
  override val emitSQL = name
  override def emit(context: Relation): Try[Int] = context.attributes.indexWhere(_.name == name) match {
    case -1 => Failure(new QueryException(s"Relation $context did not contain attribute $name"))
    case i: Int => Success(i)
  }
}

case class Comparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Row => Boolean] {

  class Predicate[A](val pred: A => Boolean) extends (A => Boolean) {
    def apply(x: A) = pred(x)

    def &&(that: A => Boolean) = new Predicate[A](x => pred(x) && that(x))

    def ||(that: A => Boolean) = new Predicate[A](x => pred(x) || that(x))

    def unary_! = new Predicate[A](x => !pred(x))
  }

  implicit def toPredicate[A](pred: A => Boolean): Predicate[A] = new Predicate(pred)

  override lazy val emitSQL = s"$left $op $right"

  override def emit(context: Relation): Try[Row => Boolean] = (for {
    leftside <- left.emit(context)
    rightside <- right.emit(context)
  } yield {
    (leftside, op, rightside) match {
      case (l: (Row => Boolean), "AND", r: (Row => Boolean)) => Success({ x: Row => l(x) && r(x)})
      case (l: (Row => Boolean), "OR", r: (Row => Boolean)) => Success({ x: Row => l(x) || r(x)})
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
}
case class ParenComparison(left: Expr[_], op: String, right: Expr[_]) extends Expr[Row => Boolean] {
  def this(c: Comparison) = this(c.left, c.op, c.right)
  override def emit(context: Relation) = new Comparison(left, op, right).emit(context)
  override lazy val emitSQL = s"($left $op $right)"
}