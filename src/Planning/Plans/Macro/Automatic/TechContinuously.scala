package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import ProxyBwapi.Techs.Tech

class TechContinuously(tech: Tech) extends Plan {
  
  description.set("Tech " + tech)
  
  override def onUpdate() {
    if (With.self.hasTech(tech)) return
    if ( ! With.units.existsOurs(tech.whatResearches)) return
    With.scheduler.request(this, Get(tech))
  }
}
