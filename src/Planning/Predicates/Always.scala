package Planning.Predicates

case class Always() extends Predicate {
  override def apply: Boolean = true
}
