package Plans.Generic.Compound

class AllParallel extends AbstractPlanCompleteAll {
  
  final override def onFrame() = {
    getChildren.foreach(_.onFrame())
  }
}
