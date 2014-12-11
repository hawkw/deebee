import deebee._
import deebee.exceptions.QueryException
import deebee.sql.SQLParser
import deebee.sql.ast._
import deebee.storage._
import org.scalatest.{BeforeAndAfterEach, GivenWhenThen, Matchers, FeatureSpec}
import scala.util.Success
import scala.io.Source
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

/**
 * Integration tests for the whole system.
 *
 * Created by hawk on 12/2/14.
 */
class IntegrationSpec extends FeatureSpec with Matchers with GivenWhenThen with BeforeAndAfterEach {

  val testdb = getClass
    .getResource("testdb")
    .getPath

  // reset the DB contents before each test
  override def beforeEach() {
    Files.write(
      Paths.get(testdb + "/Writers/Writers.csv"),
      ("1,'Isaac','Yudovich','Asimov','1/20/1920','4/6/1992','Russian SFSR'\n" +
        "2,'Robert','Anson','Heinlein','7/7/1902','5/8/1988','USA'\n" +
        "3,'Arthur','Charles','Clarke','12/16/1917','3/19/2008','USA'\n"
        ).getBytes(StandardCharsets.UTF_8)
    )
  }

  feature("SELECT statements are processed correctly.") {
    scenario("an in-memory relation receives a simple `SELECT * FROM` statement") {

      Given("a simple in-memory relation")
      val faculty = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Selectable
      When("the relation is queried")
      val query = SQLParser.parse("SELECT * FROM faculty;").get

      Then("the parser should parse the query as a SELECT statement")
      query shouldBe a [SelectStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[SelectStmt])
      result shouldBe a [Success[_]]

      And("the relation should return the correct result set")
      result.get.toString should equal (faculty.toString)

    }
    scenario("an in-memory relation receives a `SELECT` statement with projections ") {
      Given("a simple in-memory relation")
      val faculty = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Selectable
      When("the relation is queried with projections")
      val query = SQLParser.parse("SELECT first_name, last_name FROM faculty;").get

      Then("the parser should parse the query as a SELECT statement")
      query shouldBe a[SelectStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[SelectStmt])
      result shouldBe a [Success[_]]

      And("the relation should return the projected columns")
      result.get.toString should equal("|Gregory|Kapfhammer\n|Robert|Roos\n|Janyl|Jumadinova\n|John|Wenskovitch")
    }

    scenario("an in-memory relation receives a `SELECT` statement with a `WHERE` clause ") {
      Given("a simple in-memory relation")
      val faculty = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Selectable
      When("the relation is queried with a SELECT ... WHERE statement")
      val query = SQLParser.parse("SELECT * FROM faculty WHERE id > 2;").get

      Then("the parser should parse the query as a SELECT statement")
      query shouldBe a[SelectStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[SelectStmt])
      result shouldBe a [Success[_]]

      And("the results should only contain columns matching the predicate")
      result.get.toString should equal("|3|Janyl|Jumadinova|Alden 107\n|4|John|Wenskovitch|Alden 108")
    }
    scenario("an in-memory relation receives a `SELECT` statement with a `WHERE` clause and projections ") {
      Given("a simple in-memory relation")
      val faculty = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Selectable
      When("the relation is queried with a SELECT ... WHERE statement with projections")
      val query = SQLParser.parse("SELECT first_name, last_name FROM faculty WHERE first_name='Gregory';").get

      Then("the parser should parse the query as a SELECT statement")
      query shouldBe a[SelectStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[SelectStmt])
      result shouldBe a [Success[_]]

      And("the results should only contain columns matching the predicate")
      result.get.toString should equal("|Gregory|Kapfhammer")
    }
    scenario("an in-memory relation receives a `SELECT` statement with a `LIMIT` clause") {

      Given("a simple in-memory relation")
      val faculty = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Selectable
      When("the relation is queried")
      val query = SQLParser.parse("SELECT * FROM faculty LIMIT 1;").get

      Then("the parser should parse the query as a SELECT statement")
      query shouldBe a [SelectStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[SelectStmt])
      result shouldBe a [Success[_]]

      And("the result set should contain the requested number of rows")
      result.get.rows should have size 1
      And("the result set should match the query")
      result.get.toString should equal ("|1|Gregory|Kapfhammer|Alden 106")

    }
  }
  feature("DELETE statements are processed correctly.") {
    scenario("an in-memory relation receives a `DELETE` statement") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("DELETE FROM faculty;").get

      Then("the parser should parse the query as a DELETE statement")
      query shouldBe a[DeleteStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[DeleteStmt])
      result shouldBe a[Success[_]]

      And("the relation should be empty.")
      faculty = result.get
      faculty.rows should have size 0
    }
    scenario("an in-memory relation receives a `DELETE` statement with a predicate") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("DELETE FROM faculty WHERE id > 2;").get

      Then("the parser should parse the query as a DELETE statement")
      query shouldBe a[DeleteStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[DeleteStmt])
      result shouldBe a[Success[_]]

      And("the relation should not contain rows matching the predicate.")
      faculty = result.get
      faculty.rows should have size 2
      faculty.toString should not include "|3|Janyl|Jumadinova|Alden 107\n|4|John|Wenskovitch|Alden 108"
    }
    scenario("an in-memory relation receives a `DELETE` statement with a `LIMIT` clause") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(4), new VarcharEntry("John", 25), new VarcharEntry("Wenskovitch", 25), new VarcharEntry("Alden 108", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("DELETE FROM faculty LIMIT 1;").get

      Then("the parser should parse the query as a DELETE statement")
      query shouldBe a[DeleteStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[DeleteStmt])
      result shouldBe a[Success[_]]

      And("the relation should have the correct number of rows.")
      faculty = result.get
      faculty.rows should have size 3
    }
  }
  feature("INSERT statements are processed correctly.") {
    scenario("an in-memory relation receives an `INSERT INTO` statement") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("INSERT INTO faculty VALUES(4, 'John', 'Wenskovitch', 'Alden 108');").get

      Then("the parser should parse the query as an INSERT statement")
      query shouldBe an [InsertStmt]

      And("the query should be processed successfully")
      val result = faculty.process(query.asInstanceOf[InsertStmt])
      result shouldBe a[Success[_]]

      And("the relation should have the correct number of rows.")
      faculty = result.get
      faculty.rows should have size 4

      And("the relation should contain the added row")
      faculty.toString should include ("|4|John|Wenskovitch|Alden 108")
    }
    scenario("an in-memory relation recieves an `INSERT INTO` statement that violates its' integrity constraints") {
      pending
    }
    scenario("an in-memory relation recieves an `INSERT INTO` statement that violates its' type constraints") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("INSERT INTO faculty VALUES('this is a bad place for a string to be', 'John', 'Wenskovitch', 'Alden 108');").get

      Then("it should throw a QueryException with the correct message")
      the [QueryException] thrownBy faculty.process(query.asInstanceOf[InsertStmt]).get should have message "TypeError when creating Integer entry"

    }
    scenario("an in-memory relation recieves an `INSERT INTO` statement that contains the wrong number of values") {
      Given("a simple in-memory modifyable relation")
      var faculty: Relation with Modifyable = new View(
        Set[Row](
          Seq[Entry[_]](new IntegerEntry(1), new VarcharEntry("Gregory", 25), new VarcharEntry("Kapfhammer", 25), new VarcharEntry("Alden 106", 25)),
          Seq[Entry[_]](new IntegerEntry(2), new VarcharEntry("Robert", 25), new VarcharEntry("Roos", 25), new VarcharEntry("Alden 107", 25)),
          Seq[Entry[_]](new IntegerEntry(3), new VarcharEntry("Janyl", 25), new VarcharEntry("Jumadinova", 25), new VarcharEntry("Alden 107", 25))
        ),
        Seq[Attribute](
          Attribute("id", IntegerType, List(Primary_Key, Not_Null)),
          Attribute("first_name", VarcharType(25), Nil),
          Attribute("last_name", VarcharType(25), Nil),
          Attribute("office", VarcharType(25), Nil)
        )
      ) with Modifyable
      When("the relation is queried")
      val query = SQLParser.parse("INSERT INTO faculty VALUES(4, 'John', 'Wenskovitch', 'Alden 108', 'Ph.D Candidate', 100238);").get

      Then("the parser should parse the query as an INSERT statement")
      query shouldBe an [InsertStmt]

      And("the query should fail")
      val result = faculty.process(query.asInstanceOf[InsertStmt])
      result should be a 'failure

      And("the failure should contain the correct exception")
      the [QueryException] thrownBy result.get should have message "Could not insert " +
        "(4, 'John', 'Wenskovitch', 'Alden 108', 'Ph.D Candidate', 100238):\nExpected 4 values, but received 6."

      And("the relation should have the correct number of rows.")
      faculty.rows should have size 3

      And("the relation should not contain the added row")
      faculty.toString should not include "|4|John|Wenskovitch|Alden 108"


    }
  }
  feature("Queries are handled correctly at the database level.") {
    val target = new CSVDatabase("testdb", testdb)

    scenario("a CSV database recieves a `SELECT` statement") {
      Given("a CSV database")
      val conn = target.connectTo
      When("the relation is queried")
      val tried = conn.statement("SELECT * FROM Writers;")
      Then("the result should contain all the rows from the table")
      tried should be a 'success
      val result = tried.get
      result should be ('defined)
      result.get.rows should have size 3
      And("the result should contain the correct rows")
      val tableString = result.toString
      tableString should include("|1|Isaac|Yudovich|Asimov|1/20/1920|4/6/1992|Russian SFSR")
      tableString should include("|2|Robert|Anson|Heinlein|7/7/1902|5/8/1988|USA")
      tableString should include("|3|Arthur|Charles|Clarke|12/16/1917|3/19/2008|USA")
    }

    scenario("a CSV database receives an `INSERT` statement") {
      Given("a CSV database")
      val conn = target.connectTo
      When("the relation is queried")
      val tried = conn.statement("INSERT INTO Writers VALUES(4, 'Ray', 'Douglas', 'Bradbury', '8/22/1920', '6/5/2012', 'USA');")
      Then("the query result should be successful")
      tried should be a 'success
      val result = tried.get
      result should not be 'defined
      And("SELECTing from the database should contain the correct rows")
      val tableString = conn.statement("SELECT * FROM Writers;").get.toString
      tableString should include("|1|Isaac|Yudovich|Asimov|1/20/1920|4/6/1992|Russian SFSR")
      tableString should include("|2|Robert|Anson|Heinlein|7/7/1902|5/8/1988|USA")
      tableString should include("|3|Arthur|Charles|Clarke|12/16/1917|3/19/2008|USA")
      tableString should include("|4|Ray|Douglas|Bradbury|8/22/1920|6/5/2012|USA")
      And("the CSV file on disk should contain the correct contents")
      val back = Source.fromFile( testdb + "/Writers/Writers.csv"). mkString
      back should include ("1,'Isaac','Yudovich','Asimov','1/20/1920','4/6/1992','Russian SFSR'\n" +
        "2,'Robert','Anson','Heinlein','7/7/1902','5/8/1988','USA'\n" +
        "3,'Arthur','Charles','Clarke','12/16/1917','3/19/2008','USA'\n" +
        "4,'Ray','Douglas','Bradbury','8/22/1920','6/5/2012','USA'")
    }

    scenario("a CSV database receives an `INSERT` statement that violates its' integrity constraints") {
      Given("a CSV database")
      val conn = target.connectTo
      When("the relation is queried with an INSERT statement that violates a PRIMARY KEY constraint")

      val tried = conn.statement("INSERT INTO Writers VALUES(1, 'Ray', 'Douglas', 'Bradbury', '8/22/1920', '6/5/2012', 'USA');")
/*
      Then("the query result should be a failure")
      tried should be a 'failure
      And("it should have the correct query exception")
      the [QueryException] thrownBy tried.get should have message "Could not insert, violation of UNIQUE constraint"
      */
      And("SELECTing from the database should not contain the correct rows")
      val tableString = conn.statement("SELECT * FROM Writers;").get.toString
      tableString should not include("|1|Ray|Douglas|Bradbury|8/22/1920|6/5/2012|USA")
    }

    scenario("a CSV database receives a `DELETE` statement") {
      Given("a CSV database")
      val conn = target.connectTo
      When("the relation is queried")
      val tried = conn.statement("DELETE FROM Writers WHERE id > 2;")
      Then("the query result should be successful")
      tried should be a 'success
      val result = tried.get
      result should not be 'defined
      And("SELECTing from the database should contain the correct rows")
      val table = conn.statement("SELECT * FROM Writers;").get.get
      table.rows should have size 2
      val tableString = table.toString
      tableString should include("|1|Isaac|Yudovich|Asimov|1/20/1920|4/6/1992|Russian SFSR")
      tableString should include("|2|Robert|Anson|Heinlein|7/7/1902|5/8/1988|USA")
      tableString should not include("|3|Arthur|Charles|Clarke|12/16/1917|3/19/2008|USA")
      tableString should not include("|4|Ray|Douglas|Bradbury|8/22/1920|6/5/2012|USA")
      And("the CSV file on disk should contain the correct contents")
      val back = Source.fromFile( testdb + "/Writers/Writers.csv"). mkString
      back should not include ("3,'Arthur','Charles','Clarke','12/16/1917','3/19/2008','USA'\n" +
        "4,'Ray','Douglas','Bradbury','8/22/1920','6/5/2012','USA'")
    }
  }
  feature("CREATE TABLE statements are processed correctly.") {
    scenario("a CSV database recieves a `CREATE` statement") {
      pending
    }
  }

}
