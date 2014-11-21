import deebee.sql.ast.Node
import org.scalatest.{FlatSpec, Matchers}
import deebee.sql.SQLParser

import scala.util.{Try, Success}

/**
 * Tests for the SQL parser
 * Created by hawk on 11/22/14.
 */
class ParserSpec extends FlatSpec with Matchers {
  "The SQL Parser" should "parse a simple CREATE TABLE sequence" in {
    val create = "CREATE TABLE Test ( test INTEGER PRIMARY KEY NOT NULL );"
    val result: Try[Node] = SQLParser.parse(create)
    result shouldBe a [Success[Node]]
    result.get.emitSQL should include (create)
  }
}
