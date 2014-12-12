package deebee
package exceptions

/**
 * Exceptions generated as a result of an invalid internal state.
 * These are generally fatal.
 * @author Hawk Weisman
 *
 * Created by hawk on 11/19/14.
 */
class InternalStateException(message:String, cause: Option[Throwable] = None) extends DeeBeeException(message, cause)