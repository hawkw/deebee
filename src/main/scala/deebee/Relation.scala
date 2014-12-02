package deebee

import deebee.exceptions.QueryException
import deebee.sql.ast.Attribute

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

class View(
            val rows: Set[Row],
            val attributes: Seq[Attribute[_]]
            ) extends Relation