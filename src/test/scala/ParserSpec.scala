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

  private def writersSchema = Source fromURL (getClass getResource "/Writers.sql") getLines()

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
      "\ttestdec DECIMAL(1, 2),\n" +
      "\ttestchar CHAR(15)\n" +
      ");"
    )
  }

  it should "parse the first CREATE TABLE from Writers.sql, ignoring comments" in {
    val result: Try[Node] = SQLParser.parse(writersSchema take 15 mkString "\n")
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
  it should "parse a CREATE TABLE statement from Writers.sql with FOREIGN KEY constraints" in {
    val result: Try[Node] = SQLParser.parse(writersSchema slice (27,33)  mkString "\n")
    result shouldBe a [Success[Node]]
    result.get.emitSQL should include (
    "CREATE TABLE Contemporaries (\n" +
      "\twriter_id INTEGER NOT NULL,\n" +
      "\tcontemporary_id INTEGER NOT NULL,\n" +
      "\tFOREIGN KEY (writer_id) REFERENCES Writers (id)\n" +
      "\tFOREIGN KEY (contemporary_id) REFERENCES Writers (id)\n" +
      ");"
    )
  }

  it should "parse a DROP TABLE statement" in {
    assertReconstructed("DROP TABLE test;")
  }

  it should "parse a simple SELECT statement" in {
    assertReconstructed("SELECT * FROM test;")
  }

  it should "parse a SELECT statement with projections" in {
    assertReconstructed("SELECT test1, test2 FROM test;")
  }

  it should "parse a SELECT statement with a WHERE clause" in {
    assertReconstructed("SELECT * FROM test WHERE test1 = 0;")
  }

  it should "parse a SELECT statement with a chained WHERE clause" in {
    assertReconstructed("SELECT * FROM test WHERE test1 = 9 AND test2 = 5;")
  }
  it should "parse a SELECT statement with a parenthesized WHERE clause" in {
    assertReconstructed("SELECT * FROM suppliers WHERE (city = 'New York' AND name = 'IBM') OR (ranking >= 10);")
  }

  it should "parse a SELECT statement with a LIMIT clause" in {
    assertReconstructed("SELECT * FROM test LIMIT 5;")
  }

  it should "parse a SELECT statement with WHERE and LIMIT clauses" in {
    assertReconstructed("SELECT * FROM test WHERE test1 = 9 LIMIT 5;")
  }

  it should "parse a DELETE statement" in {
    assertReconstructed("DELETE FROM test;")
  }
  it should "parse a DELETE statement with a WHERE clause" in {
    assertReconstructed("DELETE FROM test WHERE test9 = 'deleteme';")
  }

  it should "parse a DELETE statement with a LIMIT clause" in {
    assertReconstructed("DELETE FROM test LIMIT 8;")
  }

  it should "parse a DELETE statement with WHERE and LIMIT clauses" in {
    assertReconstructed("DELETE FROM test WHERE test2 > 3 LIMIT 100;")
  }

  it should "parse a basic INSERT statement" in {
    assertReconstructed("INSERT INTO test VALUES (1, 'a string', 2, 'another string');")
  }
}
