package Planning.Predicates.Compound

import Planning.Predicate

case class Check(lambda:() => Boolean) extends Predicate {
  override def apply: Boolean = lambda()
}
