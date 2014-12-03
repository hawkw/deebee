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
  def filter(predicate: Row => Boolean): Relation = new View(
    rows.filter(predicate),
    attributes
  )
  protected def filterNot(predicate: Row => Boolean): Try[Relation with Modifyable]
  protected def drop(n: Int): Try[Relation with Modifyable]

  def iterator = rows.toIterator

  def take(n: Int) = new View(rows.take(n), attributes)
  override def toString = rows.map(r => r.foldLeft("")((acc, thing) => acc + "|" + thing)).mkString("\n")
}

trait Selectable extends Relation {

  def process(select: SelectStmt): Try[Relation] = {
    val predicate = select.where
      .map(clause => clause.emit(this))
    (select.projections match {
      case GlobProj :: Nil => Success(if (predicate.isDefined) {
        this.filter(predicate.get.get)
      } else this)
      case Nil => Failure(new QueryException("Received a SELECT statement with no projections."))
      case p: Seq[Proj] if p.length > 0 => Success((if (predicate.isDefined) {
        this.filter(predicate.get.get)
      } else this).project(p.map(_.emit)))
    }).map(results =>
      results.take(
        select.limit
          .map(_
          .emit(this)
          .get // this will be Success because it's a constant.
          )
          .getOrElse(results.rows.size)
      )
      )
  }

}
trait Modifyable extends Relation with Selectable {
  def process(insert: InsertStmt): Try[Relation with Selectable with Modifyable] = insert match {
    case InsertStmt(_, vals: List[Const[_]]) if vals.length == attributes.length => add(Try(
      (for { i <- 0 until vals.length } yield {
        attributes(i).apply(vals(i).emit(this).get)
      }).map{t: Try[Entry[_]] => t.get}
    ).get)
    case InsertStmt(_, vals) => Failure(new QueryException(s"Could not insert (${vals.mkString(", ")}):\n" +
      s"Expected ${attributes.length} values, but received ${vals.length}."))
  }
  def process(delete: DeleteStmt): Try[Relation with Selectable with Modifyable] = delete match {
    case DeleteStmt(_, None, None) => drop(rows.size)
    case DeleteStmt(_, Some(comp), None) => (for (pred <- comp.emit(this)) yield filterNot(pred)).flatten
    case DeleteStmt(_, None, Some(limit)) => (for (n <- limit.emit(this)) yield drop(n)).flatten
    case DeleteStmt(_, Some(comp), Some(limit)) => ??? //TODO: Implement
  }

}

/**
 * An immutable, in-memory [[Relation]].
 * @param rows
 * @param attributes
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

  override protected def filterNot(predicate: (Row) => Boolean): Try[Relation with Selectable with Modifyable] = Success(
    new View(rows.filterNot(predicate), attributes)with Selectable with Modifyable
  )

  override protected def drop(n: Int): Try[Relation with Selectable with Modifyable] = Success(
    new View(rows.drop(n), attributes) with Modifyable
  )
}