package Plans.Generic.Compound

import Traits.TraitSettableChildren

class AllSimultaneous
  extends AbstractPlanCompleteAll
  with TraitSettableChildren {
  
  final override def children = getChildren
  
  final override def onFrame() = {
    children.foreach(_.onFrame())
  }
}
