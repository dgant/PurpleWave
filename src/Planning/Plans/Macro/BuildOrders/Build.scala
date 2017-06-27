package Planning.Plans.Macro.BuildOrders

import Macro.BuildRequests.BuildRequest
import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With

class Build(initialBuildables: Seq[BuildRequest] = Vector.empty) extends Plan {
  
  def this(someBuildable: BuildRequest) = this(Vector(someBuildable))
  
  val buildables = new Property[Seq[BuildRequest]](initialBuildables)
  
  override def onUpdate() {
    description.set("Build " + buildables.get.take(3).map(_.toString).mkString(", ") + (if (buildables.get.size > 3) "..." else ""))
    With.scheduler.request(this, buildables.get)
  }
}
