package Planning.Predicates.Compound

import Planning.Predicates.Predicate

case class Check(lambda:() => Boolean) extends Predicate {
  override def apply: Boolean = lambda()
}
