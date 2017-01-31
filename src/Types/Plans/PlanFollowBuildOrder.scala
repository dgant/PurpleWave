package Types.Plans

import Startup.With
import Types.BuildOrders.BuildOrder
import bwapi.UnitType

import scala.collection.JavaConverters._
import scala.collection.mutable

class PlanFollowBuildOrder extends PlanParallel {
  
  val buildOrder = new BuildOrder
  
  override def _onInitialization() {
    super._onInitialization()
    
    val have = _defaultZeroMutableHashMap
    val need = _defaultZeroMutableHashMap
    val planned = _defaultZeroMutableHashMap
  
    With.game.self.getUnits.asScala.groupBy(_.getType).foreach(pair => have.put(pair._1, pair._2.size))
  
    _children = buildOrder
      .getUnitTypes
      .map(unitType => {
        need(unitType) += 1
        if (need(unitType) > have(unitType) + planned(unitType)) {
          planned(unitType) += 1
          return Some(_createABuildPlan(unitType))
        } else {
          return None
        }})
    }
  
  def _defaultZeroMutableHashMap():mutable.HashMap[UnitType, Integer] = {
    new mutable.HashMap[UnitType, Integer]() { override def default(key:UnitType) = 0 }
  }
  
  def _createABuildPlan(product: UnitType):Plan = {
    val builder = product.whatBuilds.first
    
    if (builder.isBuilding) {
      return new PlanBuildUnitFromBuilding(builder, product)
    }
    if (builder.isWorker) {
      return new PlanBuildBuildingWithWorker(builder, product)
    }
    
    throw new Exception("Don't know how to build this yet.")
  }
}
