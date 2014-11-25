/**
 * Created by hawk on 11/25/14.
 */
package object deebee {
  type Row = List[deebee.storage.Entry[_]]
  type Column = Stream[deebee.storage.Entry[_]]
  type ResultSet = Stream[Row]
}
