package deebee.schema

/**
 * Created by hawk on 11/20/14.
 */
sealed abstract class Constraint
case object PrimaryKey extends Constraint
case object ForeignKey extends Constraint
case object NotNull extends Constraint
case object Unique extends Constraint

