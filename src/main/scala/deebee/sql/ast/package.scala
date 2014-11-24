package deebee.sql

import scala.language.implicitConversions

/**
 * Created by hawk on 11/21/14.
 */
package object ast {
  type Predicate = Expr[Boolean]
  implicit def liftIdent(name: String): Ident = Ident(name)
  implicit def unwrapIdent(i: Ident): String = i.name
  implicit def liftConstant[T](x: T): Const[T] = Const(x)
  implicit def unliftConstant[T](c: Const[T]): T = c.x
}
