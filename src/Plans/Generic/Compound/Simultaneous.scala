package Plans.Generic.Compound

import Traits.TraitSettableChildren

class Simultaneous
  extends AbstractPlanCompleteAll
  with TraitSettableChildren {
  
  final override def children = getChildren
  
  final override def onFrame() = {
    children.foreach(_.onFrame())
  }
}
