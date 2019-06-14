package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Milestones._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class OneBaseIslandCarrier extends GameplanTemplate {

  override def scoutPlan: Plan = NoPlan()
  override def workerPlan: Plan = NoPlan()

  protected def extraSpaceZone: Zone = {
    val mainZone = With.geography.ourMain.zone
    val other = mainZone.exit.map(_.otherSideof(mainZone))
    val output = other.getOrElse(With.geography.ourNatural.zone)
    output
  }
  override def placementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(Protoss.Pylon, preferZone = Some(With.geography.ourMain.zone)),
      new Blueprint(Protoss.Pylon, preferZone = Some(extraSpaceZone))
  )}

  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
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
      Get(Protoss.Forge)),
    new SwitchEnemyRace(
      whenTerran  = new BuildOrder(Get(1, Protoss.PhotonCannon), Get(2, Protoss.Nexus), Get(2, Protoss.PhotonCannon), Get(2, Protoss.Carrier)),
      whenProtoss = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(2, Protoss.Carrier), Get(2, Protoss.Nexus)),
      whenZerg    = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(2, Protoss.Corsair), Get(5, Protoss.PhotonCannon)),
      whenRandom  = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(2, Protoss.Corsair), Get(5, Protoss.PhotonCannon))))


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

    new If(
      new UnitsAtLeast(8, Protoss.Carrier),
      new RequireMiningBases(2)),

    new UpgradeContinuously(Protoss.CarrierCapacity),
    new PumpRatio(Protoss.Corsair, 0, 12, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Terran.Wraith, 1.0))),
    new Pump(Protoss.Carrier, 4, 30),

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
