package deebee

import deebee.exceptions.QueryException
import deebee.sql.ast.{SelectStmt, Proj, GlobProj, Attribute}

import scala.util.{Try, Failure, Success}

/**
 * Created by hawk on 11/29/14.
 */
trait Relation {

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  def rows: Set[Row]
  def attributes: Seq[Attribute[_]]
  def project(names: Seq[String]): Relation = new View(
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
  def iterator = rows.toIterator

  def take(n: Int) = new View (rows.take(n), attributes)

  override def toString = rows.map(r => r.foldLeft("")((acc, thing) => acc + "|" + thing)).mkString("\n")
}

trait Selectable extends Relation {
  def processSelect(select: SelectStmt): Try[Relation] = {
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

class View(
            val rows: Set[Row],
            val attributes: Seq[Attribute[_]]
            ) extends Relation