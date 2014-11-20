package deebee.schema

/**
 * Created by hawk on 11/20/14.
 */
sealed abstract class Datatype
case object Integer extends Datatype
case class Char(n: Int) extends Datatype
case class Varchar(n: Int) extends Datatype
case class Numeric(p: Int, s: Int) extends Datatype
case class Decimal(p: Int, s: Int) extends Datatype
case object DateType extends Datatype