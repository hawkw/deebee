package deebee.storage

import java.io.File
import scala.io.Source
import scala.language.postfixOps

import akka.actor.TypedProps
import akka.actor.TypedActor

import deebee.Database
import deebee.sql.SQLParser
import deebee.sql.ast._

/**
 * A database backed by CSVs on disk
 * Created by hawk on 11/24/14.
 */
class CSVDatabase(name: String, val path: String) extends Database(name) {
  override type Table = RelationActor
  val back = new File(path)
  // init logic
  if ((back exists) && (back isDirectory)) {
    // if the database already exists, re-initialize
    logger.info(s"Database $name already existed on dissc, reinitializing.")
    for (dir <- back.listFiles if dir isDirectory) {
      val code = new File(dir, "schema.sql")
      if (code exists) {
        val schema: CreateStmt = SQLParser.parse(Source fromFile code mkString)
          .get // todo: ew
          .asInstanceOf[CreateStmt]
        tables += (schema.name.toString -> create(schema))
        logger.info(s"Created table ${schema.name}")
      }
    }
  } else {
    // this is a new database
    logger.info(s"Database $name did not exist on disc, initializing.")
    back.mkdirs()
  }

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  override protected def create(c: CreateStmt): Table =
    TypedActor(system)
      .typedActorOf(
        TypedProps(classOf[RelationActor],
          new CSVRelation(c, path)),
          c.name
      )
}
