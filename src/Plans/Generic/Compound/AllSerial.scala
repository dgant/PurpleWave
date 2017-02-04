package Plans.Generic.Compound

import Traits.TraitSettableChildren

class AllSerial
  extends AbstractPlanCompleteAll
  with TraitSettableChildren {
  
  final override def children = getChildren
  
  final override def onFrame() {
    var continue = true
    children
      .foreach(child => {
        if (continue) {
          child.onFrame()
          continue &&= child.isComplete
        }
      })
  }
}
