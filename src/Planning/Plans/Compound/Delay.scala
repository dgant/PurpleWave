package Planning.Plans.Compound

import Lifecycle.With
import Planning.Plan

class Delay(frames: Int, child: Plan) extends Plan {

  var firstFrame: Option[Int] = None
  
  override def onUpdate() {
    firstFrame = firstFrame.orElse(Some(With.frame))
    if (With.framesSince(firstFrame.get) >= frames) {
      child.update()
    }
  }
}
