package deebee
package storage

import java.io.File
import scala.util.{Try, Success, Failure}
import deebee.sql.ast.{Attribute, Constraint}

/**
 * Implementation for a [[Relation]] backed by a comma-separated values file.
 *
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
   * @return a [[Try]] on a reference to a [[Relation]] with the row appended
   */
  override protected def add(row: deebee.Row): Try[Relation] = ???
}