package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.{DefendFFEWithProbesAgainst4Pool, DefendFFEWithProbesAgainst9Pool, PlacementForgeFastExpand}
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchType, UnitMatchWorkers}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvZFFEEconomic, PvZGatewayFE}

class PvZFFE extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvZGatewayFE, PvZFFEEconomic)
  override val completionCriteria: Predicate = new Latch(new And(new BasesAtLeast(2), new UnitsAtLeast(1, Protoss.CyberneticsCore)))
  
  override def scoutPlan: Plan = new If(
    new Not(new EnemyStrategy(With.fingerprints.fourPool)),
    new ScoutOn(Protoss.Pylon))
  
  override def placementPlan: Plan = new PlacementForgeFastExpand
  
  override def buildOrderPlan: Plan = new If(
    new Employing(PvZGatewayFE),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool),
      new BuildOrder(ProtossBuilds.PvZFFE_GatewayForgeCannonsConservative: _*),
      new If(
        new EnemyStrategy(With.fingerprints.twelvePool, With.fingerprints.tenHatch),
        new BuildOrder(ProtossBuilds.PvZFFE_GatewayForgeCannonsEconomic: _*),
        new BuildOrder(ProtossBuilds.PvZFFE_GatewayNexusForge: _*))),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new BuildOrder(ProtossBuilds.PvZFFE_Vs4Pool: _*),
      new If(
        new EnemyStrategy(With.fingerprints.twelveHatch),
        new Trigger(
          new UnitsAtMost(0, Protoss.Forge),
          new BuildOrder(ProtossBuilds.PvZFFE_NexusGatewayForge: _*),
          new BuildOrder(ProtossBuilds.PvZFFE_NexusForgeCannons: _*)),
        new If(
          new EnemyStrategy(With.fingerprints.twelvePool),
          new BuildOrder(ProtossBuilds.PvZFFE_NexusForgeCannons: _*),
          new If(
            new EnemyStrategy(With.fingerprints.overpool),
            new BuildOrder(ProtossBuilds.PvZFFE_ForgeNexusCannon: _*),
            new BuildOrder(ProtossBuilds.PvZFFE_ForgeCannonNexus: _*))))))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fourPool, With.fingerprints.ninePool),
        new UnitsAtLeast(8, Protoss.Probe)),
      new Pump(Protoss.Zealot)),
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
        new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.overpool),
        new FrameAtLeast(GameTime(3, 0)()),
        new FrameAtMost(GameTime(6, 0)()),
        new UnitsAtMost(2, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new DefendFFEWithProbesAgainst9Pool)
  )

  override def workerPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(20, UnitMatchWorkers),
      new Build(
        Get(1, Protoss.Assimilator),
        Get(1, Protoss.CyberneticsCore))),
    new PumpWorkers,
    new Pump(Protoss.Dragoon, maximumTotal = 1),
    new Pump(Protoss.Zealot),
    new Build(
      Get(1, Protoss.Pylon),
      Get(1, Protoss.Forge)),
    new RequireMiningBases(2),
    new Build(
      Get(2, Protoss.Nexus),
      Get(1, Protoss.Gateway)),
    new Build(
      Get(2, Protoss.Pylon),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)),
    new RequireMiningBases(2)
  )
}
