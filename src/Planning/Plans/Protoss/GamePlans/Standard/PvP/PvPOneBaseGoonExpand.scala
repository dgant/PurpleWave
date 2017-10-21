package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOneBaseGoonExpand

class PvPOneBaseGoonExpand extends GameplanModeTemplatePvP {
  
  override val activationCriteria : Plan      = new Employing(PvPOneBaseGoonExpand)
  override val completionCriteria : Plan      = new OnMiningBases(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override def buildPlans = Vector(
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new SafeAtHome),
      new Parallel(
        new PvPIdeas.BuildDragoonsOrZealots,
        new Build(RequestAtLeast(4, Protoss.Gateway))),
      new RequireMiningBases(2)))
}
