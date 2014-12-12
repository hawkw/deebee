package deebee
package frontends

import deebee.storage.CSVDatabase
import sql._
import ast._

import scala.util.Try

/**
 * Common interface for connections into a DeeBee database.
 *
 * == Connecting to a DeeBee DB ==
 *
 * [[Connection]]s are created using the [[Connection.apply()]] method on the
 * [[Connection]] companion object. This creates a connection into the specified
 * database. If that database is not currently running, it will be started up
 * (or created if it doesn't exist).
 *
 * Currently, all connections are [[BlockingConnection]]s
 * that block on queries until they recieve a result. Eventually, there will be non-blocking
 * connections available as well.
 *
 * Also planned is a more advanced connections API, allowing callers to specify additional
 * configuration options to configure both the connection and the database it connects to.
 * A JDBC driver for DeeBee databases is also under consideration.
 *
 * == Using Connections ==
 *
 * Connections expose one main method, [[Connection.statement() statement()]].
 * This method returns a [[scala.util.Try Try]] containing a [[scala.Option Option]] on a
 * [[deebee.Relation Relation]] containing the result set.
 *
 * If the query failed,  [[Connection.statement() statement()]] will return a
 * [[scala.util.Failure Failure]] containing a
 * [[deebee.exceptions.QueryException QueryException]] with a message describing the failure.
 *
 * If the query was successful, [[Connection.statement() statement()]] will return a
 * [[scala.util.Success Success]] containing a [[scala.Option Option]] on a
 * [[deebee.Relation Relation]]. If the query was a `SELECT` statement, the
 * option will be defined; otherwise, it will be [[scala.None]]
 *
 * The result set returned by a `SELECT` statement is a [[deebee.Relation Relation]].
 * Relations provide a [[Relation.iterator iterator]] method to iterate through the rows,
 * as well as a [[Relation.rows rows]] method to access the entire set of rows.
 *
 * Eventually there will be a type-safe JDBC-style API for deconstructing rows, but
 * this is currently not yet implemented.
 *
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
  protected[deebee] def connect(into: Database): Connection = new BlockingConnection(into)

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
