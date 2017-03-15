package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

abstract class AbstractAll extends Plan {
  
  val children = new Property[List[Plan]](List.empty)
  
  override def isComplete:Boolean = getChildren.forall(_.isComplete)
  
  final override def getChildren: Iterable[Plan] = children.get
}
