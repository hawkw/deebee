package deebee
package exceptions

/**
 * Created by hawk on 11/19/14.
 */
class InternalStateException(message:String, cause: Option[Throwable] = None) extends DeeBeeException(message, cause) {

}
