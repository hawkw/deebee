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
                   ) extends RelationActor(name, attributes, constraints) {

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  override def rows: Set[Row] = ???
}