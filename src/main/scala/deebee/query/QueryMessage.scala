package deebee.query
import deebee.Table

/**
 * Messages for Query Processor <--> Table interaction
 *
 * Created by hawk on 11/19/14.
 */
sealed trait QueryMessage
case class Select(from: Table, which: List[String], predicate: from.Row => Boolean) extends QueryMessage
case class Insert(into: Table, values: into.Row) extends QueryMessage
case class Delete(from: Table, predicate: from.Row => Boolean) extends QueryMessage
case class Result(contents: Table) extends QueryMessage