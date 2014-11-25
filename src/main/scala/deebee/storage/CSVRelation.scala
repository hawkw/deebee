package deebee.storage

import java.io.File

import deebee.sql.ast.{Attribute, Constraint}
import deebee.{Column, Row}

/**
 * Created by hawk on 11/19/14.
 */
class CSVRelation(
                   name: String,
                   attributes: List[Attribute[_]],
                   constraints: List[Constraint],
                   private val back: File
                   ) extends Relation(name, attributes, constraints) {
  /**
   * Return all entries from the `n`th column
   * @param n the number of the column to select
   * @return A List of all the entries from the `n`th colum
   */
  override protected def column(n: Int): Column = ???

  /**
   * @return all columns from the table.
   */
  override protected def columns: List[Column] = ???

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  override protected def rows: Set[Row] = ???
}