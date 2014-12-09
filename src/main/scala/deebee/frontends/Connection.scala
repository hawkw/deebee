package deebee
package frontends

import akka.actor.ActorRef
import akka.pattern.{ask,AskableActorRef}
import akka.util.Timeout
import sql._
import ast._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Created by hawk on 12/9/14.
 */
class Connection(protected val into: Database) {

  def statement(queryString: String): Try[Option[Relation]] = (SQLParser parse queryString).flatMap{
    query: Node => Try(into.query(query).map(_.get))
  }
}
