package Planning.Predicates.Compound

import Planning.Predicate

class Check(lambda:() => Boolean) extends Predicate {
  override def apply: Boolean = lambda()
}
