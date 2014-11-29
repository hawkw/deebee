package deebee
package storage

import java.util.Date

/**
 * Created by hawk on 11/24/14.
 */
sealed abstract class Entry[T](val value: T){
  override def equals(that: Any): Boolean = that match {
    case that : Entry[T] => this.value == that.value
    case _ => false
  }
}

object Entry {
  implicit def unpackImplicitly[T](e: Entry[T]): T = e.value
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