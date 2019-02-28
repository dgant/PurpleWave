package Planning.Predicates.Compound

import Planning.Predicates.Never
import Planning.{Predicate, Property}
import Utilities.Forever

class Sticky(initialPredicate: Predicate = new Never, duration: Int = Forever()) extends Predicate {
  
  val predicate = new Property[Predicate](initialPredicate)

  var permanentValue: Option[Boolean] = None
  override def isComplete: Boolean = {
    if (permanentValue.isEmpty) {
      permanentValue = Some(predicate.get.isComplete)
    }
    permanentValue.get
  }
}
