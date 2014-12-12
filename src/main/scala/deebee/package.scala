/**
 * DeeBee: A Tiny Database
 *
 * @author Hawk Weisman
 * Created by hawk on 11/25/14.
 */
package object deebee {
  type Row = Seq[deebee.storage.Entry[_]]
  type Column = Stream[deebee.storage.Entry[_]]
}
