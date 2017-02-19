package Plans.Compound

class AllParallel extends AbstractPlanCompleteAll {
  
  description.set(Some("Do in parallel"))
  
  override def onFrame() = {
    getChildren.foreach(_.onFrame())
  }
}
