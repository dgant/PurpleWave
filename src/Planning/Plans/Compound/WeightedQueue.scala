package Planning.Plans.Compound

class WeightedQueue(initialChildren: WeightedPlan*) extends AbstractAll(initialChildren: _*) {
  
  override def onUpdate() {
    val childrenOrdered = children.get
      .filter(_.asInstanceOf[WeightedPlan].allowed)
      .sortBy(_.asInstanceOf[WeightedPlan].weight)
      .reverse
    
    childrenOrdered.foreach(delegate)
  }
}
