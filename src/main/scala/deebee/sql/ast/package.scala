package deebee.sql

import scala.language.implicitConversions

/**
 * Created by hawk on 11/21/14.
 */
package object ast {
  type Predicate = Expr[Boolean]
  implicit def liftString(x: String): Expr[String] = Const(x)
  implicit def unliftString(c: Const[String]): String = c.x
  implicit def liftInt(x: Int): Expr[Int] = Const(x)
  implicit def listDouble(x: Double): Expr[Double] = Const(x)
}
