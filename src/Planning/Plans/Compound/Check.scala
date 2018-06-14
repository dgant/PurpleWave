package Planning.Plans.Compound

import Planning.Predicate

class Check(lambda:() => Boolean) extends Predicate {
  
  description.set("If (lambda)")
  
  override def isComplete: Boolean = lambda()
}
