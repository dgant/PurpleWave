package Planning.Predicates.Compound

import Planning.Predicate

case class Or(children: Predicate*) extends Predicate{
  
  override def apply: Boolean = children.exists(_.apply)
}