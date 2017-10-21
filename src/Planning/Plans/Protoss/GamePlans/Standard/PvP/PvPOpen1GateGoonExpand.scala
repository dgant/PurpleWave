package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Scenarios.EnemyStrategy
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1GateGoonExpand

class PvPOpen1GateGoonExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1GateGoonExpand)
  override val completionCriteria : Plan      = new OnMiningBases(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  override val aggression         : Double    = 0.8
  
  override val buildOrder = ProtossBuilds.OpeningZCoreZ
  override def buildPlans = Vector(
    new PvPIdeas.BuildDragoonsOrZealots,
    new If(
      new Or(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprintProxyGateway),
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint2Gate)),
      new Build(RequestAtLeast(3, Protoss.Gateway))),
    new FlipIf(
      new And(
        new UnitsAtLeast(4, UnitMatchWarriors),
        new SafeAtHome),
      new Build(RequestAtLeast(4, Protoss.Gateway))),
      new RequireMiningBases(2))
}
