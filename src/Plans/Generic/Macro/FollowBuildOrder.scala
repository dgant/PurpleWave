package Plans.Generic.Macro

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Types.Property
import bwapi.UnitType

import scala.collection.mutable

class FollowBuildOrder extends Plan {
  
  description.set(Some("Follow a build order"))
  
  val buildables = new Property[Iterable[Buildable]](List.empty)
  
  var _buildsToFulfill:Iterable[Buildable] = List.empty
  val _plans = new mutable.HashMap[Buildable, Plan]
  val _unitIdsFromCompletedBuilds = new mutable.HashMap[Buildable, Int]
  
  override def getChildren: Iterable[Plan] = {
    _buildsToFulfill.filter(_plans.contains).map(_plans(_))
  }
  
  override def onFrame() {
    val buildsRecentlyCompleted   = _plans.filter(_._2.isComplete).keySet
    val buildsRecentlyDestroyed   = _unitIdsFromCompletedBuilds.filter(pair => With.unit(pair._2).isEmpty)
    val unitsWanted               = new mutable.HashMap[UnitType, Int]
    val unitsActual               = With.ourUnits.groupBy(_.getType).mapValues(_.size)
    
    _buildsToFulfill              = buildables.get
                                      .filterNot(buildsRecentlyCompleted.contains)
                                      .toList
                                      .sortBy(buildsRecentlyDestroyed.contains) //Redo destroyed builds first
                                      .filterNot(_isFulfilled(_, unitsWanted, unitsActual))
    
    val buildsThatNeedPlans = _buildsToFulfill.filterNot(_plans.contains)
    buildsThatNeedPlans.foreach(order => _plans(order) = _buildPlan(order))
    getChildren.foreach(_.onFrame())
  }
    
  def _isFulfilled(
    buildable:Buildable,
    unitsWanted:mutable.HashMap[UnitType, Int],
    unitsActual:Map[UnitType, Int])
      :Boolean = {
    if (buildable.upgrade.nonEmpty) {
      return With.game.self.getUpgradeLevel(buildable.upgrade.get) >= buildable.level
    }
    if (buildable.tech.nonEmpty) {
      return With.game.self.hasResearched(buildable.tech.get)
    }
    val unitType = buildable.unit.get
    unitsWanted.put(unitType, 1 + unitsWanted.getOrElse(unitType, 0))
    return unitsActual.getOrElse(unitType, 0) >= unitsWanted(unitType)
  }
  
  def _buildPlan(buildable:Buildable):Plan = {
    if (buildable.unit.nonEmpty) {
      val unitType = buildable.unit.get
      if (unitType.isBuilding) {
        return new BuildBuilding(unitType)
      } else {
        return new TrainUnit(unitType)
      }
    }
    if (buildable.tech.nonEmpty) {
      return new ResearchTech(buildable.tech.get)
    }
    if (buildable.upgrade.nonEmpty) {
      return new ResearchUpgrade(buildable.upgrade.get, buildable.level)
    }
    
    throw new Exception("Tried to build a Buildable that doesn't specify any unit, tech, or research")
  }
}
