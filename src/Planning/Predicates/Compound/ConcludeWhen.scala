package Planning.Predicates.Compound

import Planning.Predicate

class ConcludeWhen(permanencePredicate: Predicate, valuePredicate: Predicate) extends Predicate {

  var isPermanent: Boolean = false
  var permanentValue: Option[Boolean] = None

  override def isComplete: Boolean = {
    isPermanent = isPermanent || permanencePredicate.isComplete

    if (permanentValue.isEmpty || ! isPermanent) {
      permanentValue = Some(valuePredicate.isComplete)
    }

    permanentValue.get
  }
}
