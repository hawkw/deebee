package deebee.sql

import scala.language.implicitConversions

/**
 * Created by hawk on 11/21/14.
 */
package object ast {
  type Predicate = Expr[Boolean]
  implicit def liftIdent(name: String): Ident = Ident(name)
  implicit def liftConstant[T](x: T): Expr[T] = Const(x)
  implicit def unliftConstant[T](c: Const[T]): T = c.x
}
