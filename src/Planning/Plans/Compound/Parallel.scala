package Planning.Plans.Compound

import Planning.Plan

class Parallel(children: Plan*) extends Plan {
  override def onUpdate() { children.foreach(_.update()) }
}
