package Planning.Plans.Placement

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Requests.{RequestProduction, Get}
import Planning.Plan
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.UpgradeComplete
import ProxyBwapi.Races.Protoss

class PlacementPolicy

class FFETest extends Plan {

  lazy val ourNatural = Some(With.geography.ourNatural.zone)
  lazy val ourMain = Some(With.geography.ourMain.zone)

  lazy val requestFFE = ChainBlueprints(
    new Blueprint(Protoss.Pylon,            requireZone = ourNatural),
    new Blueprint(Protoss.Gateway,          requireZone = ourNatural),
    new Blueprint(Protoss.Forge,            requireZone = ourNatural),
    new Blueprint(Protoss.PhotonCannon,     requireZone = ourNatural),
    new Blueprint(Protoss.PhotonCannon,     requireZone = ourNatural),
    new Blueprint(Protoss.Nexus,            requireZone = ourNatural),
    new Blueprint(Protoss.Pylon,            requireZone = ourMain),
    new Blueprint(Protoss.Assimilator,      preferZone  = ourMain),
    new Blueprint(Protoss.CyberneticsCore,  preferZone  = ourMain),
    new Blueprint(Protoss.Gateway,          preferZone  = ourMain),
    new Blueprint(Protoss.Gateway,          preferZone  = ourMain),
    new Blueprint(Protoss.Gateway,          preferZone  = ourMain),
    new Blueprint(Protoss.Gateway,          preferZone  = ourMain))

  override def onUpdate(): Unit = {
    With.groundskeeper.suggest(requestFFE)
  }
}

class PvEFFETestBuild extends GameplanTemplate {
  override def scoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def buildOrder: Seq[RequestProduction] = Seq(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(11, Protoss.Probe),
    Get(Protoss.Forge),
    Get(14, Protoss.Probe),
    Get(2, Protoss.PhotonCannon),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Nexus),
    Get(Protoss.Gateway),
    Get(16, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(17, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(18, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(19, Protoss.Probe),
    Get(Protoss.Zealot),
  )

  override def buildPlans: Seq[Plan] = Seq(
    new FFETest,
    new RequireMiningBases(2),
    new If(
      new Not(new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.Dragoon.buildFrames)),
      new Pump(Protoss.Zealot)),
    new Build(
      Get(Protoss.DragoonRange),
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator)),
    new Pump(Protoss.Dragoon),
    new Build(Get(8, Protoss.Gateway))
  )
}
