package Plans.Generic.Compound

class AllSimultaneous extends AbstractPlanCompleteAll {
  
  final override def onFrame() = {
    getChildren.foreach(_.onFrame())
  }
}
