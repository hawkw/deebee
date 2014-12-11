package deebee

import deebee.exceptions.QueryException
import deebee.sql.ast._
import deebee.storage.Entry

import scala.util.{Try, Failure, Success}

/**
 * Common operations for all Relations.
 *
 * @author Hawk Weisman <hawk@meteorcodelabs.com>
 *
 * Created by hawk on 11/29/14.
 */
trait Relation {
  protected implicit lazy val table = this

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  def rows: Set[Row]

  def attributes: Seq[Attribute]

  /**
   * Add a new [[Row]] to this [[Relation]], returning a [[Try]] on a [[Relation]]with the row appended.
   *
   * In the case of mutable [[Relation]]s (i.e. [[deebee.storage.CSVRelation CSVRelation]]), this
   * can be a reference back to the same relation after it has been modified; while in the course of
   * immutable relations (i.e. [[View]]) this can be a new relation with those rows appended. This
   * is intended to ensure that the code for processing `INSERT` statements is polymorphic
   * across mutable and immutable [[Relation]]s.
   *
   * If the relation cannot be modified, this returns a [[Try]] containing an
   * [[deebee.exceptions.InternalStateException]].
   *
   * @param row the row to add
   * @return a [[Try]] on a reference to a [[Relation]] with the row appended
   */
  protected def add(row: Row): Try[Relation with Modifyable]

  def project(names: Seq[String]): Relation with Selectable = new View(
    rows.map(
      r => names.map(
        name => {
          r(attributes.indexWhere(a => a.name.name == name) match {
            case n if n < 0 => throw new QueryException(s"Could not process projection, $this did not contain $name")
            case n: Int => n
          })
        }
        )
      ),
    attributes.filter(names contains _.name.name)
  )
  def filter(predicate: Row => Boolean): Relation with Selectable with Modifyable = new View(
    rows.filter(predicate),
    attributes
  ) with Selectable with Modifyable

  protected def filterNot(predicate: Row => Boolean): Relation with Selectable with Modifyable = new View(
    rows.filterNot(predicate),
    attributes
  ) with Selectable with Modifyable

  /**
   * Return a copy of the relation with the first `n` rows dropped.
   *
   * @param n the number of rows to drop
   * @return a copy of the relation with the first `n` rows dropped.
   */
  //TODO: this should return a row set (so as to avoid the overhead of creating a whole new Relation)
  protected def drop(n: Int): Try[Relation with Selectable with Modifyable]

  def iterator = rows.toIterator

  def take(n: Int) = new View(rows.take(n), attributes)
  override def toString = rows.map(r => r.foldLeft("")((acc, thing) => acc + "|" + thing)).mkString("\n")
}

trait Selectable extends Relation {

  def process(select: SelectStmt): Try[Relation] = {
    val predicate = select.where
      .map(clause => clause.emit(this))
    (select.projections match {
      case GlobProj :: Nil => Success(predicate match {
        case Some(Success(pred)) => this.filter(pred)
        case _ => this
      })
      case Nil => Failure(new QueryException("Received a SELECT statement with no projections."))
      case p: Seq[Proj] if p.length > 0 => Success((predicate match {
        case Some(Success(pred)) => this.filter(pred)
        case _ => this
      }).project(p.map(_.emit)))
    }).map(results =>
      results.take(
        select.limit
          .map(_
            .emit(this)
            .getOrElse(results.rows.size) // this will be Success because it's a constant.
          )
          .getOrElse(results.rows.size)
      )
      )
  }

}
trait Modifyable extends Relation with Selectable {
  /**
   * Join a `Seq[Try[T]]` into a `Try[Seq[T]]`.
   *
   * This really should be in the stdlib.
   * @param xs a Sequence of Try[T]s
   * @tparam T the type which the Try wraps
   * @return a `Try[Seq[T]]` containing either the values or
   *         the first exception of the failure encountered
   */
  protected def sequence[T](xs : Seq[Try[T]]) : Try[Seq[T]] = (Try(Seq[T]()) /: xs) {
        // TODO: rewrite this as eta expansion on Seq[Try]
    (a, b) => a flatMap (c => b map (d => c :+ d))
  }
  /**
   * Attempt to constrain a value against an [[Attribute]]
   * and create an [[Entry]], returning either a Success
   * containing the created [[Entry]] or a Failure containing
   * a [[QueryException]] representing the constraints violation.
   * @param value the value to make into an entry
   * @param attr the attribute against which to make the entry
   * @return the results, either a `Success(Entry)` or a `Failure(QueryException)`
   */
  protected def mkEntry (value: Const[_], attr: Attribute): Try[Entry[_]] = {
    if (
      ((attr.constraints contains Not_Null) || (attr.constraints contains Primary_Key))
        && value.isInstanceOf[NullConst[_]]) {
      Failure(new QueryException("Could not insert, violation of NOT NULL constraint"))
    } else if (
      ((attr.constraints contains Unique) || (attr.constraints contains Primary_Key))
        && project(Seq(attr.name))
          .rows
          .exists(r =>
            r.exists(_.value == value.x)
        )
    ) {
        Failure(new QueryException("Could not insert, violation of UNIQUE constraint"))
      } else {
        attr(value).emit(this).flatten
    }
  }

  /**
   * Processes an `INSERT` statement, returning the result relation.
   * The returned relation is either a pointer to this relation
   * (in the case of mutable relations) or a new copy with the
   * changes (in the case of immutable relations).
   * @param insert the AST of the insert statement to process
   * @return A `Try[Relation]` containing either the result or
   *         any [[QueryException]] that occurred during processing.
   */
  def process(insert: InsertStmt): Try[Relation with Selectable with Modifyable] = insert match {
    case InsertStmt(_, vals: List[Const[_] @unchecked]) if vals.length == attributes.length =>
      sequence(
        for { i <- 0 until vals.length } yield {
          mkEntry(vals(i), attributes(i))
        }
      ).flatMap(add)
    case InsertStmt(_, vals) => Failure(new QueryException(s"Could not insert (${vals.mkString(", ")}):\n" +
      s"Expected ${attributes.length} values, but received ${vals.length}."))
  }

  /**
   * Processes a  `DELETE` statement, returning the result relation.
   * The returned relation is either a pointer to this relation
   * (in the case of mutable relations) or a new copy with the
   * changes (in the case of immutable relations).
   * @param delete the AST of the delete statement to process
   * @return A `Try[Relation]` containing either the result or
   *         any [[QueryException]] that occurred during processing.
   */
  def process(delete: DeleteStmt): Try[Relation with Selectable with Modifyable] = delete match {
    case DeleteStmt(_, None, None) => drop(rows.size)
    case DeleteStmt(_, Some(comp), None) => for (pred <- comp.emit(this)) yield filterNot(pred)
    case DeleteStmt(_, None, Some(limit)) => (for (n <- limit.emit(this)) yield drop(n)).flatten
    case DeleteStmt(_, Some(comp), Some(limit)) => for {
      pred <- comp.emit(this)
      n <- limit.emit(this)
    } yield {
      var count = 0
      filterNot{ r =>
        if(pred(r))  count = count +1
        pred(r) && count != n
     }
    }
  }

}

/**
 * An immutable, in-memory [[Relation]].
 * @param rows the rows that make up this relation
 * @param attributes the attributes of the relation
 */
class View(
            val rows: Set[Row],
            val attributes: Seq[Attribute]
            ) extends Relation with Selectable with Modifyable {
  /**
   * Add a new [[Row]] to this [[Relation]], returning a [[Try]] on a [[Relation]]with the row appended.
   *
   * In the case of mutable [[Relation]]s (i.e. [[deebee.storage.CSVRelation C S V R e l a t i o n]]), this
   * can be a reference back to the same relation after it has been modified; while in the course of
   * immutable relations (i.e. [[View]]) this can be a new relation with those rows appended. This
   * is intended to ensure that the code for processing `INSERT` statements is polymorphic
   * across mutable and immutable [[Relation]]s.
   *
   * If the relation cannot be modified, this returns a [[Try]] containing an
   * [[deebee.exceptions.InternalStateException]].
   *
   * @param row the row to add
   * @return a reference to a [[Relation]] with the row appended
   */
  override protected def add(row: Row): Try[Relation with Selectable with Modifyable] = Success(
    new View(rows + row, attributes)
  )
/*
  override protected def filterNot(predicate: (Row) => Boolean): Try[Relation with Selectable with Modifyable] = Success(
    new View(rows.filterNot(predicate), attributes)with Selectable with Modifyable
  )
*/
  override protected def drop(n: Int): Try[Relation with Selectable with Modifyable] = Success(
    new View(rows.drop(n), attributes) with Modifyable
  )
}