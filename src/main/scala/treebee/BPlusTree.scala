package treebee

import scala.collection.{SortedSetLike, SortedSet}

/**
 * Created by hawk on 12/29/14.
 */
trait BPlusTree[K <: Ordering[K],V] {

  protected val root: Branch[K]

  def find(key: K): V = find(key, root)

  protected def find(key: K, node: Node[_]): V = node match {
    case Leaf(value: V) => value
    case Branch(children) => ??? //todo: figure this out
  }

}
abstract class Node[A]

case class Branch[K <: Ordering[K]](children: Map[K,Node[K]]) extends Node[K]

case class Leaf[A](contents: A) extends Node[A]