package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Planning.Plan
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Protoss

abstract class GameplanModeTemplatePvP extends GameplanModeTemplate {
  
  override val completionCriteria : Plan = new Or(new UnitsAtLeast(2, Protoss.Nexus))
  override val defaultScoutPlan   : Plan = new If(new UnitsAtLeast(1, Protoss.CyberneticsCore), new Scout)
  override def defaultAttackPlan  : Plan = new PvPIdeas.AttackSafely
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
}
  