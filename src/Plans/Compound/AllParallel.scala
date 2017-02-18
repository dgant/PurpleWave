package Plans.Compound

class AllParallel extends AbstractPlanCompleteAll {
  
  override def onFrame() = {
    getChildren.foreach(_.onFrame())
  }
}
