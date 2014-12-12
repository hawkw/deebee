package deebee
package exceptions

/**
 * Exceptions that may occur as a result of a failed query attempt.
 *
 * @author Hawk Weisman
 * Created by hawk on 11/24/14.
 */
class QueryException(message: String, cause: Option[Throwable] = None) extends DeeBeeException(message, cause) {
  def this(message: String, cause: Throwable) = this(message, Some(cause))
}

class QueryParsingException(message:String) extends DeeBeeException(message, None)