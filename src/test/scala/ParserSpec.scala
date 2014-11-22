import deebee.sql.ast.Node
import org.scalatest.{FlatSpec, Matchers}
import deebee.sql.SQLParser

import scala.util.{Try, Success}

/**
 * Tests for the SQL parser
 * Created by hawk on 11/22/14.
 */
class ParserSpec extends FlatSpec with Matchers {

  /**
   * Helper method for testing. Asserts that the a SQL query can be reconstructed from its'
   * AST representation (which should more or less ensure that it was parsed correctly).
   * @param sql the SQL query to test
   */
  private def assertReconstructed (sql: String): Unit = {
    val result: Try[Node] = SQLParser.parse(sql)
    result shouldBe a [Success[Node]]
    result.get.emitSQL should include (sql)
  }
  "The SQL Parser" should "parse a simple CREATE TABLE statement" in {
    assertReconstructed(
      "CREATE TABLE Test (\n" +
        "\ttest INTEGER PRIMARY KEY NOT NULL" +
      "\n);"
    )
  }

  it should "parse a CREATE TABLE statement with multiple attributes and types" in {
    assertReconstructed("CREATE TABLE Test (\n" +
      "\ttestint INTEGER PRIMARY KEY NOT NULL,\n" +
      "\ttestvarchar VARCHAR(255),\n" +
      "\ttestnumeric NUMERIC(5, 8) NOT NULL,\n" +
      "\ttestchar CHAR(15)\n" +
      ");"
    )
  }
}
