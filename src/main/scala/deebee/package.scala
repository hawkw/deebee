/**
 * == DeeBee: A Tiny Database ==
 *
 * [[deebee.frontends]] contains the documentation for the various
 * ways of connecting to DeeBee. If you want to use DeeBee as a
 * library in your project, check out [[deebee.frontends.Connection]]
 * for the connection-manager API. If you want to interact with DeeBee
 * from the command-line, just run the DeeBee jarfile.
 *
 * DeeBee is released under the MIT license.
 *
 * @author Hawk Weisman
 * Created by hawk on 11/25/14.
 */
package object deebee {
  type Row = Seq[deebee.storage.Entry[_]]
  type Column = Stream[deebee.storage.Entry[_]]
}
