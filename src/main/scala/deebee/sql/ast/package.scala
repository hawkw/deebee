package deebee
package sql

import scala.language.implicitConversions

/**
 * Abstract Syntax Tree (AST) for DeeBee SQL queries.
 *
 * All AST nodes extend the [[ast.Node Node]] trait. There are subtypes
 * for specific types of SQL statements([[ast.DMLStmt DMLStmt]],
 * [[ast.CreateStmt CreateStmt]], and [[ast.DropStmt DropStmt]]).
 *
 * Most of the classes here are for internal use only. The only time
 * I can see anyone needing to poke around in here is if you wanted
 * to implement LISP-esque macros in DeeBee SQL, which would actually
 * be kind of cool, but I'd question your sanity.
 *
 * Created by hawk on 11/21/14.
 */
package object ast {
  type Predicate = Expr[Boolean]
  implicit def liftIdent(name: String): Ident = Ident(name)
  implicit def unwrapIdent(i: Ident): String = i.name
  implicit def liftConstant[T](x: T): Const[T] = Const(x)
  implicit def unliftConstant[T](c: Const[T]): T = c.x
}
