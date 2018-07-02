package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Milestones._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class OneBaseIslandCarrier extends GameplanModeTemplate {

  override def defaultScoutPlan: Plan = NoPlan()
  override def defaultWorkerPlan: Plan = NoPlan()

  protected def extraSpaceZone: Zone = {
    val mainZone = With.geography.ourMain.zone
    val other = mainZone.exit.map(_.otherSideof(mainZone))
    val output = other.getOrElse(With.geography.ourNatural.zone)
    output
  }
  override def defaultPlacementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourMain.zone)),
      new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(extraSpaceZone)),
      new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourMain.zone)),
      new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(extraSpaceZone))
  )}

  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(12, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(13, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(16, Protoss.Probe),
    Get(Protoss.AirDamage),
    Get(18, Protoss.Probe),
    Get(Protoss.Stargate),
    Get(20, Protoss.Probe),
    Get(3, Protoss.Pylon),
    Get(22, Protoss.Probe),
    Get(Protoss.FleetBeacon),
    Get(2, Protoss.Stargate),
    Get(24, Protoss.Probe),
    Get(4, Protoss.Pylon),
    Get(Protoss.Forge),
    Get(2, Protoss.Nexus),
    Get(3, Protoss.PhotonCannon),
    Get(2, Protoss.Carrier))

  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAt(1000),
    new Pump(Protoss.Probe, 21),
    new BuildGasPumps,

    new Trigger(
      new Or(
        new EnemyHasShown(Terran.Wraith),
        new EnemyHasShown(Protoss.Arbiter),
        new EnemyHasShown(Protoss.ArbiterTribunal)),
      new Build(
        Get(1, Protoss.RoboticsFacility),
        Get(1, Protoss.Observatory),
        Get(2, Protoss.Observer))),

    new UpgradeContinuously(Protoss.CarrierCapacity),
    new PumpMatchingRatio(Protoss.Corsair, 0, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new Pump(Protoss.Carrier),

    new If(
      new UpgradeComplete(Protoss.AirDamage, 3),
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),

    new Trigger(
      new UnitsAtLeast(4, Protoss.Carrier),
      new RequireMiningBases(2)),
    new Pump(Protoss.Probe),
    new BuildCannonsAtNatural(2),
    new BuildCannonsAtExpansions(2),
    new IfOnMiningBases(2, new Build(Get(4, Protoss.Stargate)))
  )
}
