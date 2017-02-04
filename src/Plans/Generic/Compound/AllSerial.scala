package Plans.Generic.Compound

class AllSerial extends AbstractPlanCompleteAll {
  
  final override def onFrame() {
    var continue = true
    getChildren
      .foreach(child => {
        if (continue) {
          child.onFrame()
          continue &&= child.isComplete
        }
      })
  }
}
