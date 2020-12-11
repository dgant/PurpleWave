package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZIdeas.Eject4PoolScout
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvZFFEConservative, PvZFFEEconomic, PvZGatewayFE}
import Utilities.GameTime

class PvZFFE extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvZGatewayFE, PvZFFEConservative, PvZFFEEconomic)
  override val completionCriteria: Predicate = new Latch(new And(new BasesAtLeast(2), new UnitsAtLeast(1, Protoss.CyberneticsCore)))
  
  override def initialScoutPlan: Plan = new If(
    new Not(new EnemyStrategy(With.fingerprints.fourPool)),
    new If(
      new Employing(PvZFFEConservative),
      new ScoutOn(Protoss.PhotonCannon, quantity = 2),
      new ScoutOn(Protoss.Pylon)))
  
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
        new Employing(PvZFFEConservative),
        new BuildOrder(ProtossBuilds.PvZFFE_Conservative: _*),
        new If(
          new EnemyStrategy(With.fingerprints.twelveHatch),
          new Trigger(
            new UnitsAtMost(0, Protoss.Forge),
            new BuildOrder(ProtossBuilds.PvZFFE_NexusGatewayForge: _*),
            new BuildOrder(ProtossBuilds.PvZFFE_ForgeNexusCannon: _*)),
          new If(
            new EnemyStrategy(With.fingerprints.overpool, With.fingerprints.twelvePool, With.fingerprints.tenHatch),
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
      new EnemyStrategy(With.fingerprints.fourPool),
      new Pump(Protoss.PhotonCannon, 6)),
    new Trigger(
      new Or(
        new UnitsAtLeast(2, Protoss.PhotonCannon),
        new FrameAtLeast(GameTime(2, 20)())),
      new PvZIdeas.ConditionalDefendFFEWithProbesAgainst4Pool),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new EjectScout(Protoss.Probe)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Eject4PoolScout,
    new Build(
      Get(Protoss.Pylon),
      Get(Protoss.Forge),
      Get(2, Protoss.PhotonCannon),
      Get(2, Protoss.Nexus),
      Get(2, Protoss.Pylon),
      Get(Protoss.Gateway),
      Get(2, Protoss.PhotonCannon)),
    new If(
      new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.tenHatch),
      new Pump(Protoss.PhotonCannon, 4)),
    new Pump(Protoss.Dragoon, maximumTotal = 1),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Forge)),
    new RequireMiningBases(2),
    new Build(
      Get(Protoss.Gateway),
      Get(2, Protoss.Pylon),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
  )
}
