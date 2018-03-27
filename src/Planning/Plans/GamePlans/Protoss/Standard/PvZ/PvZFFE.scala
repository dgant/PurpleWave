package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.{DefendFFEWithProbesAgainst4Pool, DefendFFEWithProbesAgainst9Pool, PlacementForgeFastExpand}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{FrameAtLeast, FrameAtMost, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvZEarlyFFEConservative, PvZEarlyFFEEconomic}

class PvZFFE extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(PvZEarlyFFEEconomic, PvZEarlyFFEConservative)
  override val completionCriteria: Plan = new Latch(new And(new UnitsAtLeast(1, Protoss.CyberneticsCore)))
  
  override def defaultScoutPlan: Plan = new If(
    new And(
      new Not(new Employing(PvZEarlyFFEConservative)),
      new Not(new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool))),
    new ScoutAt(6))
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def defaultBuildOrder: Plan = new If(
    new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
    new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
    new If(
      new Employing(PvZEarlyFFEConservative),
      new BuildOrder(ProtossBuilds.FFE_Conservative: _*),
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint12Hatch),
        new BuildOrder(ProtossBuilds.FFE_NexusGatewayForge: _*),
        new BuildOrder(ProtossBuilds.FFE_ForgeCannonNexus: _*))))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.AddEarlyCannons,
    new If(
      new And(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
        new UnitsAtLeast(1, Protoss.Forge)),
      new Build(RequestAtLeast(4, Protoss.PhotonCannon))),
    new If(
      new And(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
        new FrameAtLeast(GameTime(2, 5)()),
        new FrameAtMost(GameTime(5, 0)()),
        new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
        new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
      new DefendFFEWithProbesAgainst4Pool),
    new If(
      new And(
        new EnemyStrategy(
          With.intelligence.fingerprints.fingerprint9Pool,
          With.intelligence.fingerprints.fingerprintOverpool),
        new FrameAtLeast(GameTime(3, 0)()),
        new FrameAtMost(GameTime(6, 0)()),
        new UnitsAtMost(2, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new DefendFFEWithProbesAgainst9Pool)
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Protoss.Zealot),
    new Build(
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(2, Protoss.PhotonCannon),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new RequireMiningBases(2)
  )
}
