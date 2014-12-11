package deebee
package frontends

import deebee.storage.CSVDatabase
import sql._
import ast._

import scala.util.Try

/**
 * Common interface for connections into a DeeBee database.
 *
 * @author Hawk Weisman
 * Created by hawk on 12/9/14.
 */
trait Connection {
  def statement(queryString: String): Try[Option[Relation]]
  def name: String
}
object Connection {

  private val cache = collection.mutable.Map[String,Database]()
  /**
   * Factory method for Connections. For now, this just makes [[BlockingConnection]]s.
   */
  def connect(into: Database): Connection = new BlockingConnection(into)

  /**
   * Canonical way to connect to a Database. If the database isn't already running,
   * it will be initialized in the current directory.
   * @param name the name of the database
   * @return a Connection into the specified database
   */
  def apply(name: String): Connection = apply(name, System.getProperty("user.dir"))

  /**
   * Canonical way to connect to a Database at a specified location in the filesystem.
   * If the database doesn't already exist, it will be created at the specified path.
   * @param name the name of the database
   * @param path a path to the specified database on the filesystem
   * @return a Connection into the specified database
   */
  def apply(name: String, path: String): Connection = cache
    //TODO: eventually, connection strings for different type of DBs
    .getOrElseUpdate(name, new CSVDatabase(name, path + "/name"))
    .connectTo
}

/**
 * A basic, blocking connection into a database
 * @param into The database this is a connection into
 */
class BlockingConnection(protected val into: Database) extends Connection {
  override lazy val name = into.name
  override def statement(queryString: String): Try[Option[Relation]] = (SQLParser parse queryString).flatMap{
    query: Node => into.query(query)
  }
}
