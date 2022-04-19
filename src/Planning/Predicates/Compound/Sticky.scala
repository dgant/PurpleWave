package Planning.Predicates.Compound

import Planning.Predicates.{Never, Predicate}
import Utilities.Property
import Utilities.Time.Forever

case class Sticky(initialPredicate: Predicate = new Never, duration: Int = Forever()) extends Predicate {
  
  val predicate = new Property[Predicate](initialPredicate)

  var permanentValue: Option[Boolean] = None
  override def apply: Boolean = {
    if (permanentValue.isEmpty) {
      permanentValue = Some(predicate.get.apply)
    }
    permanentValue.get
  }
}
