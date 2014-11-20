package deebee

/**
 * Created by hawk on 11/20/14.
 */
class Schema(val name: String, val attributes: Map[String, Column]) {

}

case class Column(t: Datatype, constraints: List[Constraint])

sealed abstract class Constraint
case object PrimaryKey extends Constraint
case object ForeignKey extends Constraint
case object NotNull extends Constraint
case object Unique extends Constraint

sealed abstract class Datatype
case object Integer extends Datatype
case class Char(n: Int) extends Datatype
case class Varchar(n: Int) extends Datatype
case class Numeric(p: Int, s: Int) extends Datatype
case class Decimal(p: Int, s: Int) extends Datatype