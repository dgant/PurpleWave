package Planning.Predicates

case class Never() extends Predicate {
  override def apply: Boolean = false
}
