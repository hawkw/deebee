package deebee.storage

import deebee.Database
import deebee.sql.ast.CreateStmt

/**
 * Created by hawk on 11/24/14.
 */
class CSVDatabase(name: String) extends Database(name) {
  override type Table = CSVRelation

  /**
   * Generate the correct type of relation for this type of database.
   * This adds actors to the actor system.
   * @return
   */
  override protected def create(c: CreateStmt): Table = ???
}
