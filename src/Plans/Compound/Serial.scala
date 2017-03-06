package Plans.Compound

class Serial extends AbstractAll {
  
  description.set("Do in series")
  
  override def onFrame() {
    var continue = true
    getChildren.foreach(child => if (continue) { child.onFrame(); continue = child.isComplete })
  }
}
