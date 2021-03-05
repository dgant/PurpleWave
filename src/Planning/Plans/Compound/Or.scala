package Planning.Plans.Compound

import Planning.Predicate

class Or(children: Predicate*) extends Predicate{
  
  override def apply: Boolean = children.exists(_.apply)
}