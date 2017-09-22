package Planning.Plans.Protoss.GamePlans.Standard.PvZ

import Planning.Plan
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Always
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.BuildHuggingNexus
import ProxyBwapi.Races.Protoss

class PvZOneGateGas extends TemplateMode {
  
  override val activationCriteria: Plan = new Always
  override val completionCriteria: Plan = new UnitsAtLeast(3, Protoss.Pylon)
  
  override val buildOrder = ProtossBuilds.ZZCoreZ
  override val placementPlans = Vector(new BuildHuggingNexus)
}
