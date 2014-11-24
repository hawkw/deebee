package deebee
package exceptions

/**
 * Created by hawk on 11/19/14.
 */
class DeeBeeException(message:String, cause: Option[Throwable]) extends Exception(message) {
  cause.foreach(this.initCause(_))
}