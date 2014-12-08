package deebee
package storage

import java.io.File
import com.github.tototoshi.csv.{CSVWriter, CSVReader}
import deebee.sql.SQLParser
import deebee.sql.ast._

import scala.util.{Try, Success, Failure}
import scala.io.Source
import deebee.sql.ast.{CreateStmt, Attribute, Constraint}

/**
 * Implementation for a [[Relation]] backed by a comma-separated values file.
 *
 * Created by hawk on 11/19/14.
 */
class CSVRelation(
                   schema: CreateStmt,
                   path: String
                   ) extends Relation with Selectable with Modifyable {
  def this(name: String, path: String) = this(
    SQLParser.parse(
      Source
        .fromFile(s"$path/$name/schema.sql")
        .mkString
    )
      .get
      .asInstanceOf[CreateStmt],
    path)

  val name = schema.name
  val attributes = schema.attributes
  val constraints = schema.constraints

  val back = new File(s"$path/$name/$name.csv")
  if (!back.exists()) back.mkdirs()

  // ugly hack for persisting schemas
  val schemaBack = new File(s"$path/$name/schema.sql")
  if (!schemaBack.exists()) {
    val pw = new java.io.PrintWriter(schemaBack)
    try pw.write(schema.emitSQL) finally pw.close()
  }

  def reader = CSVReader.open(back)
  def writer = CSVWriter.open(back, append = true)

  /**
   * Selects the whole table (unordered)
   * @return the whole table, as a set of lists of entries
   */
  override def rows: Set[Row] =
    reader
      .all()
      .toSet
      .map { row: List[String] =>
        for {
          i <- 0 until row.length
        } yield {
          attributes(i)
            .apply(SQLParser.parseLit(row(i)).get)
            .get
      }
    }


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
  override protected def add(row: deebee.Row): Try[Relation with Modifyable] = {
    writer.writeRow(row)
    writer.close()
    Success(this)
  }

  override protected def drop(n: Int): Try[Relation with Modifyable] = {
    //todo: make this not awful
    val writer = CSVWriter.open(back)
    rows.drop(n).foreach(r => writer.writeRow(r))
    Success(this)
  }
}