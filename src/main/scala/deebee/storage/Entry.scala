package deebee
package storage

import java.util.Date

/**
 * Representation of an entry from a database row.
 *
 * These may show up when unpacking result sets. You can get the value of an
 * entry using `Entry.value` or [[Entry.unapply]] (`entry(thing)` in a pattern match).
 * Eventually there will be a typesafe JDBC-esque (`rs.nextChar`-style) API for result
 * set unpacking, but that's still a work in progress.
 *
 * @author Hawk Weisman
 * Created by hawk on 11/24/14.
 */
sealed abstract class Entry[T](val value: T){
  override def equals(that: Any): Boolean = that match {
    case that : Entry[T] => this.value == that.value
    case _ => false
  }
  override def hashCode = value.hashCode
  override def toString = value.toString
}

object Entry {
  implicit def unpackImplicitly[T](e: Entry[T]): T = e.value
  def unapply[T](e: Entry[T]) = e.value
}

class CharEntry(chars: String, length: Int) extends Entry[String](
  if (chars.length < length) {
    chars + " " * (length - chars.length)
  } else {
    chars.take(length)
  }
)

class VarcharEntry(chars: String, length: Int) extends Entry[String](chars.take(length))

class IntegerEntry(value: Int) extends Entry[Int](value)

class DecimalEntry(value: Double, val p: Int, val d: Int) extends Entry[Double](value)

class DateEntry(value: Date) extends Entry[Date](value)

class NullEntry extends Entry[Any](None)