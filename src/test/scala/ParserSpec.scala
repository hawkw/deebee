import deebee.sql.ast.Node
import org.scalatest.{WordSpec, FlatSpec, Matchers}
import deebee.sql.SQLParser
import scala.io.Source

import scala.util.{Try, Success}

/**
 * Unit tests for the SQL parser
 * Created by hawk on 11/22/14.
 */
class ParserSpec extends WordSpec with Matchers {

  private def writersSchema = Source fromURL (getClass getResource "/Writers.sql") getLines()

  /**
   * Helper method for testing. Asserts that the a SQL query can be reconstructed from its'
   * AST representation (which should more or less ensure that it was parsed correctly).
   * @param sql the SQL query to test
   */
  private def assertReconstructed (sql: String): Unit = {
    val result: Try[Node] = SQLParser.parse(sql)
    result shouldBe a [Success[_]]
    result.get.emitSQL should include (sql.replace("NUMERIC", "DECIMAL"))
  }

  "The SQL Parser" when {
    "parsing DDL CREATE and DROP statements" should {
      "parse a simple CREATE TABLE statement" in {
        assertReconstructed(
          "CREATE TABLE Test (\n" +
            "\ttest INTEGER PRIMARY KEY NOT NULL" +
            "\n);"
        )
      }

      "parse a CREATE TABLE statement with multiple attributes and types" in {
        assertReconstructed("CREATE TABLE Test (\n" +
          "\ttestint INTEGER PRIMARY KEY NOT NULL,\n" +
          "\ttestvarchar VARCHAR(255),\n" +
          "\ttestnumeric NUMERIC(5, 8) NOT NULL,\n" +
          "\ttestdec DECIMAL(1, 2),\n" +
          "\ttestchar CHAR(15)\n" +
          ");"
        )
      }

      "parse the first CREATE TABLE from Writers.sql, ignoring comments" in {
        val result: Try[Node] = SQLParser.parse(writersSchema take 19 mkString "\n")
        result shouldBe a[Success[_]]
        result.get.emitSQL should include(
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
      "parse a CREATE TABLE statement from Writers.sql with FOREIGN KEY constraints" in {
        val result: Try[Node] = SQLParser.parse(writersSchema slice(31, 37) mkString "\n")
        result shouldBe a[Success[_]]
        result.get.emitSQL should include(
          "CREATE TABLE Contemporaries (\n" +
            "\twriter_id INTEGER NOT NULL,\n" +
            "\tcontemporary_id INTEGER NOT NULL,\n" +
            "\tFOREIGN KEY (writer_id) REFERENCES Writers (id)\n" +
            "\tFOREIGN KEY (contemporary_id) REFERENCES Writers (id)\n" +
            ");"
        )
      }
      "parse a DROP TABLE statement" in {
        assertReconstructed("DROP TABLE test;")
      }
    }
    "parsing DML SELECT statements" should {


      "parse a simple SELECT statement" in {
        assertReconstructed("SELECT * FROM test;")
      }

      "parse a SELECT statement with projections" in {
        assertReconstructed("SELECT test1, test2 FROM test;")
      }

      "parse a SELECT statement with a WHERE clause" in {
        assertReconstructed("SELECT * FROM test WHERE test1 = 0;")
      }

      "parse a SELECT statement with a chained WHERE clause" in {
        assertReconstructed("SELECT * FROM test WHERE test1 = 9 AND test2 = 5;")
      }
      "parse a SELECT statement with a parenthesized WHERE clause" ignore {
        assertReconstructed("SELECT * FROM suppliers WHERE (city = 'New York' AND name = 'IBM') OR (ranking >= 10);")
      }

      "parse a SELECT statement with a LIMIT clause" in {
        assertReconstructed("SELECT * FROM test LIMIT 5;")
      }

      "parse a SELECT statement with WHERE and LIMIT clauses" in {
        assertReconstructed("SELECT * FROM test WHERE test1 = 9 LIMIT 5;")
      }
    }
    "parsing DML DELETE statements" should {
      "parse a DELETE statement" in {
        assertReconstructed("DELETE FROM test;")
      }
      "parse a DELETE statement with a WHERE clause" in {
        assertReconstructed("DELETE FROM test WHERE test9 = 'deleteme';")
      }

      "parse a DELETE statement with a LIMIT clause" in {
        assertReconstructed("DELETE FROM test LIMIT 8;")
      }

      "parse a DELETE statement with WHERE and LIMIT clauses" in {
        assertReconstructed("DELETE FROM test WHERE test2 > 3 LIMIT 100;")
      }
    }
    "parsing DML INSERT statements" should {
      "parse a basic INSERT statement" in {
        assertReconstructed("INSERT INTO test VALUES (1, 'a string', 2, 'another string');")
      }
    }
  }
}







