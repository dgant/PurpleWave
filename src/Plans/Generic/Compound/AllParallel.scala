package Plans.Generic.Compound

class AllParallel extends AbstractPlanCompleteAll {
  
  override def onFrame() = {
    getChildren.foreach(_.onFrame())
  }
}
