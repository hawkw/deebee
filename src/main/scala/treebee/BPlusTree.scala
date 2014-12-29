package treebee

import scala.collection.{SortedSetLike, SortedSet}

/**
 * Created by hawk on 12/29/14.
 */
trait BPlusTree[K,V] {

  protected val root: Branch[K]

  def find(key: K): V = find(key, root)

  protected def find(key: K, node: Node): V = node match {
    case Leaf(value: V) => value
  }

}
abstract class Node[A]

case class Branch[A](children: Map[A,Node[_]], next: Option[Branch[A]]) extends Node[A]

case class Leaf[A](contents: A) extends Node[A]