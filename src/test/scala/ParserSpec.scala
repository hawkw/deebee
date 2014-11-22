import deebee.sql.ast.Node
import org.scalatest.{FlatSpec, Matchers}
import deebee.sql.SQLParser
import scala.io.Source

import scala.util.{Try, Success}

/**
 * Tests for the SQL parser
 * Created by hawk on 11/22/14.
 */
class ParserSpec extends FlatSpec with Matchers {

  val writersSchema = Source fromURL (getClass getResource "/Writers.sql") getLines

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

  it should "parse the first CREATE TABLE from Writers.sql, ignoring comments" in {
    val lines15 = writersSchema take 15 mkString "\n"
    val result: Try[Node] = SQLParser.parse(lines15)
    result shouldBe a [Success[Node]]
    result.get.emitSQL should include (
    "CREATE TABLE Writers (\n" +
      "\tid INTEGER NOT NULL PRIMARY KEY,\n" +
      "\tfirst_name VARCHAR(15) NOT NULL,\n" +
      "\tmiddle_name VARCHAR(15),\n" +
      "\tlast_name VARCHAR(15) NOT NULL,\n" +
      "\tbirth_date VARCHAR(10) NOT NULL,\n" +
      "\tdeath_date VARCHAR(10),\n" +
      "\tcountry_of_origin VARCHAR(20) NOT NULL\n" +
      ");"
    )
  }
}
