package deebee.schema

/**
 * Created by hawk on 11/20/14.
 */
class Schema(val name: String, val attributes: Map[String, Column]) {

}

case class Column(t: Datatype, constraints: List[Constraint])


