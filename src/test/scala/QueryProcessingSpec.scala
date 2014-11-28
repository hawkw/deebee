import deebee.storage.Relation
import org.scalamock.scalatest.MockFactory
import org.scalatest.{WordSpec, FunSpec, Matchers, FlatSpec}

/**
 * Created by hawk on 11/27/14.
 */
class QueryProcessingSpec extends WordSpec with Matchers with MockFactory {
  "A SELECT statement" when {
    "in the simplest form (SELECT * FROM table)" should {
      "produce the correct result" in {
        pending
      }
    }
    "with a list of projections (SELECT one, two FROM table)" should {
      "produce the correct result" in {
        pending
      }
    }
    "with a WHERE clause and the glob projection (SELECT * FROM table WHERE cond)" should {
        "produce the correct result" in {
          pending
        }
      "with a WHERE clause and a list of projections (SELECT one, two FROM table WHERE cond)" should {
        "produce the correct result" in {
          pending
        }
      }
    }
  }
}
