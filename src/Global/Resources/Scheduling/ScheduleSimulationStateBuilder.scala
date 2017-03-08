package Global.Resources.Scheduling

import Startup.With
import Types.BwapiTypes.{TechTypes, UpgradeTypes}
import Types.UnitInfo.FriendlyUnitInfo
import bwapi.{Order, UnitType, UpgradeType}

import scala.collection.{breakOut, mutable}

object ScheduleSimulationStateBuilder {
  def build:ScheduleSimulationState = {
    val unitsOwned      = unitCount(false)
    val unitsAvailable  = unitCount(true)
    val techsOwned      = TechTypes.all.filter(With.game.self.hasResearched).to[mutable.HashSet]
    val upgradesOwned   = UpgradeTypes.all.map(upgrade => (upgrade, With.game.self.getUpgradeLevel(upgrade))).toMap
    val upgradesOwnedMutable: mutable.Map[UpgradeType, Int] = upgradesOwned.map(identity)(breakOut)
  
    //TODO: We need to include things that we're currently building/working on!
  
    new ScheduleSimulationState(
      frame           = With.game.getFrameCount,
      minerals        = With.game.self.minerals,
      gas             = With.game.self.gas,
      supplyAvailable = With.game.self.supplyTotal - With.game.self.supplyUsed,
      unitsOwned      = unitsOwned,
      unitsAvailable  = unitsAvailable,
      techsOwned      = techsOwned,
      upgradeLevels   = upgradesOwnedMutable)
  }
  
  def unitCount(requireAvailable:Boolean):collection.mutable.HashMap[UnitType, Int] = {
    collection.mutable.HashMap(
      With.units.ours
        .filter(unit => ! requireAvailable || isAvailable(unit))
        .groupBy(_.utype)
        .map(pair => (pair._1, pair._2.size)).toSeq: _*)
  }
  
  def isAvailable(unit:FriendlyUnitInfo):Boolean = {
    unit.complete && ! macroOrders.contains(unit.order)
  }
  
  val macroOrders = Set(
    Order.ArchonWarp,
    Order.BuildAddon,
    Order.BuildingLiftOff,
    Order.BuildingLand,
    Order.BuildNydusExit,
    Order.CompletingArchonSummon,
    Order.ConstructingBuilding,
    Order.CreateProtossBuilding,
    Order.DarkArchonMeld,
    Order.IncompleteBuilding,
    Order.LiftingOff,
    Order.PlaceBuilding,
    Order.Train,
    Order.TrainFighter,
    Order.WarpIn,
    Order.ZergBirth,
    Order.ZergBuildingMorph,
    Order.ZergUnitMorph)
  
}
