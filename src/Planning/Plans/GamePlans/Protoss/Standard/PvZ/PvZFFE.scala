package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{Latch, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.{DefendFFEWithProbesAgainst4Pool, DefendFFEWithProbesAgainst9Pool, PlacementForgeFastExpand}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{FrameAtLeast, FrameAtMost, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvZEarlyFFEConservative, PvZEarlyFFEEconomic}

class PvZFFE extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(PvZEarlyFFEEconomic, PvZEarlyFFEConservative)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(1, Protoss.CyberneticsCore)))
  
  override def defaultScoutPlan: Plan = new If(
    new And(
      new Not(new Employing(PvZEarlyFFEConservative)),
      new Not(new EnemyStrategy(With.fingerprints.fourPool))),
    new ScoutAt(6))
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def defaultBuildOrder: Plan = new If(
    new EnemyStrategy(With.fingerprints.fourPool),
    new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
    new If(
      new Employing(PvZEarlyFFEConservative),
      new BuildOrder(ProtossBuilds.FFE_Conservative: _*),
      new If(
        new EnemyStrategy(With.fingerprints.twelveHatch),
        new BuildOrder(ProtossBuilds.FFE_NexusGatewayForge: _*),
        new BuildOrder(ProtossBuilds.FFE_ForgeCannonNexus: _*))))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.AddEarlyCannons,
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fourPool),
        new UnitsAtLeast(1, Protoss.Forge)),
      new Build(Get(4, Protoss.PhotonCannon))),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fourPool),
        new FrameAtLeast(GameTime(2, 5)()),
        new FrameAtMost(GameTime(5, 0)()),
        new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
        new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
      new DefendFFEWithProbesAgainst4Pool),
    new If(
      new And(
        new EnemyStrategy(
          With.fingerprints.ninePool,
          With.fingerprints.overpool),
        new FrameAtLeast(GameTime(3, 0)()),
        new FrameAtMost(GameTime(6, 0)()),
        new UnitsAtMost(2, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new DefendFFEWithProbesAgainst9Pool)
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Protoss.Zealot),
    new Build(
      Get(1, Protoss.Pylon),
      Get(1, Protoss.Forge),
      Get(2, Protoss.PhotonCannon),
      Get(2, Protoss.Nexus),
      Get(1, Protoss.Gateway),
      Get(2, Protoss.Pylon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)),
    new RequireMiningBases(2)
  )
}
