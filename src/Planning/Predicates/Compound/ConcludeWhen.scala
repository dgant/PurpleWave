package Planning.Predicates.Compound

import Planning.Predicate

class ConcludeWhen(permanencePredicate: Predicate, valuePredicate: Predicate) extends Predicate {

  var isPermanent: Boolean = false
  var permanentValue: Option[Boolean] = None

  override def apply: Boolean = {
    isPermanent = isPermanent || permanencePredicate.apply

    if (permanentValue.isEmpty || ! isPermanent) {
      permanentValue = Some(valuePredicate.apply)
    }

    permanentValue.get
  }
}
