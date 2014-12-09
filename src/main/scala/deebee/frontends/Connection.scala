package deebee
package frontends

import akka.actor.ActorRef
import akka.pattern.{ask,AskableActorRef}
import akka.util.Timeout
import deebee.sql.SQLParser

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Created by hawk on 12/9/14.
 */
class Connection(protected val into: ActorRef) {
  implicit val timeout = Timeout(5 seconds)

  def statement(stmt: String): Try[View] = for {
    query <- SQLParser.parse(stmt)
  } yield {
    Await.result(into ? query , 5 seconds).asInstanceOf[View]
  }
}
