package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.{Employing, StartPositionsAtLeast}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.{Scout, ScoutOn}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvZEarlyFFEConservative, PvZEarlyFFEEconomic, PvZEarlyFFEGatewayFirst, PvZEarlyFFENexusFirst}

class PvZFFE extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(
    PvZEarlyFFEEconomic,
    PvZEarlyFFEConservative,
    PvZEarlyFFEGatewayFirst,
    PvZEarlyFFENexusFirst)
  override val completionCriteria: Plan = new Latch(new And(new UnitsAtLeast(1, Protoss.CyberneticsCore)))
  
  override def defaultScoutPlan: Plan = new If(
    new Employing(PvZEarlyFFEConservative),
    new ScoutOn(Protoss.Nexus, quantity = 2),
    new If(
      new UnitsAtLeast(1, Protoss.Pylon),
      new If(
        new StartPositionsAtLeast(4),
        new Scout(2),
        new Scout(1))))
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def defaultBuildOrder: Plan = new If(
      new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
      new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Or(
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint9Pool),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprintOverpool),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint10Hatch9Pool)),
        new BuildOrder(ProtossBuilds.FFE_ForgeFirst: _*),
        new If(
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint12Hatch),
          new If(
            new Employing(PvZEarlyFFEGatewayFirst),
            new BuildOrder(ProtossBuilds.FFE_GatewayFirst_Aggressive: _*),
            new BuildOrder(ProtossBuilds.FFE_NexusFirst: _*)),
          new If(
            new Employing(PvZEarlyFFEConservative),
            new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
            new If(
              new Employing(PvZEarlyFFEGatewayFirst),
              new BuildOrder(ProtossBuilds.FFE_GatewayFirst_Aggressive: _*),
              new If(
                new Employing(PvZEarlyFFENexusFirst),
                new BuildOrder(ProtossBuilds.FFE_NexusFirst: _*),
                new BuildOrder(ProtossBuilds.FFE_ForgeFirst: _*)))))))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(2, Protoss.PhotonCannon),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new TrainContinuously(Protoss.Zealot),
    new RequireMiningBases(2)
  )
}
