
import deebee.{Relation, View, Row}
import deebee.storage._
import deebee.sql.ast._
import org.scalatest.{GivenWhenThen, Matchers, WordSpec}
import org.scalatest.Assertions._

/**
 * Unit tests for the query processing system
 *
 * Created by hawk on 11/27/14.
 */
class QueryProcessingSpec extends WordSpec with Matchers {

  "A relation" when {
    "in memory" should {
      val row1 = Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("a string", 16))
      val row2 = Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("another string", 16))
      val target: Relation = new View(
        Set[Row](row1, row2),
        Seq[Attribute](Attribute("test1", IntegerType, Nil), Attribute("test2", VarcharType(16), Nil))
      )

      "provide access to its' rows" in {
        target.rows shouldBe a[Set[_]]
        target.rows should not be 'empty
        target.rows should contain(row1)
        target.rows should contain(row2)
      }
      "project a new relation with the given attributes" in {
        val result = target.project(Seq("test1"))
        result.rows shouldBe a[Set[_]]
        result.rows should not be 'empty
        result.rows should contain(Seq[Entry[_]](new IntegerEntry(1)))
        result.rows should contain(Seq[Entry[_]](new IntegerEntry(2)))
      }
      "correctly filter by a given predicate, returning a new relation" in {
        val result = target.filter({ r: Row => r(0).value == 1})
        result.rows shouldBe a[Set[_]]
        result.rows should not be 'empty
        result.rows should contain(row1)
      }
    }
    "backed by a CSV" should {
      "provide access to its' rows" in {
        pending
      }
      "project a new relation with the given attributes" in {
        pending
      }
      "correctly filter by a given predicate, returning a new relation" in {
        pending
      }
    }
  }

  "A relation actor" when {
    "backed by a CSV" should {
      "return the correct results for a basic SELECT" in {
        pending
      }
      "return the correct results for a SELECT with projections" in {
        pending
      }
      "return the correct results for a SELECT with a WHERE clause" in {
        pending
      }
      "return the correct results for a SELECT with a WHERE and projections" in {
        pending
      }
    }
  }
}