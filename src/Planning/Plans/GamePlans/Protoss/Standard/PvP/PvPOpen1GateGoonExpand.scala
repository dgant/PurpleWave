package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1GateGoonExpand

class PvPOpen1GateGoonExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1GateGoonExpand)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  override val aggression         : Double    = 0.8
  
  override val buildOrder = ProtossBuilds.OpeningZCoreZ
  override def buildPlans = Vector(
    new PvPIdeas.BuildDragoonsOrZealots,
    new If(
      new PvPIdeas.Crummy2GateDetection,
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    new FlipIf(
      new And(
        new UnitsAtLeast(4, UnitMatchWarriors),
        new SafeAtHome),
      new Build(RequestAtLeast(4, Protoss.Gateway)),
      new RequireMiningBases(2))
  )
}
