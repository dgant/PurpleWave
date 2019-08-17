package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvE.PvE1BaseIslandCarrier

class PvE1BaseIslandCarrier extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvE1BaseIslandCarrier)

  override def scoutPlan: Plan = NoPlan()
  override def scoutExposPlan: Plan = NoPlan()
  override def workerPlan: Plan = NoPlan()

  override def attackPlan: Plan = new Trigger(
    new UnitsAtLeast(4, Protoss.Carrier, complete = true),
    super.attackPlan)

  override def placementPlan: Plan = new PlaceIslandPylons

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
      Get(Protoss.Forge),
      Get(24, Protoss.Probe),
      Get(Protoss.FleetBeacon),
      Get(2, Protoss.Stargate),
      Get(4, Protoss.Pylon)),
    new SwitchEnemyRace(
      whenTerran  = new BuildOrder(Get(1, Protoss.PhotonCannon), Get(2, Protoss.Nexus),   Get(2, Protoss.Carrier), Get(2, Protoss.PhotonCannon)),
      whenProtoss = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(2, Protoss.Carrier), Get(2, Protoss.Nexus)),
      whenZerg    = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(4, Protoss.Corsair), Get(5, Protoss.PhotonCannon), Get(6, Protoss.Corsair), Get(2, Protoss.Nexus)),
      whenRandom  = new BuildOrder(Get(3, Protoss.PhotonCannon), Get(2, Protoss.Corsair), Get(5, Protoss.PhotonCannon))))


  override def buildPlans: Seq[Plan] = Vector(
    new PvEIslandCarrierLateGame
  )
}
