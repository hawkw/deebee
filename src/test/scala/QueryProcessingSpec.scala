
import org.scalatest.{GivenWhenThen, Matchers, WordSpec}

/**
 * Created by hawk on 11/27/14.
 */
class QueryProcessingSpec extends WordSpec with Matchers with GivenWhenThen {

  "A Relation" when {
    "in memory" should {
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
}
