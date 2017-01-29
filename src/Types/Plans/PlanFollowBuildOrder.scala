package Types.Plans

import Startup.With
import Types.BuildOrders.BuildOrder
import bwapi.UnitType

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PlanFollowBuildOrder extends Plan() {
  val buildOrder = new BuildOrder
  var _children:ListBuffer[Plan] = ListBuffer.empty
  
  override def children(): Iterable[Plan] = {
    _children
  }
  
  override def update() {
    if (! _children.isEmpty) {
      return
    }
    
    _children = ListBuffer.empty
  
    val have = _defaultZeroMutableHashMap
    val need = _defaultZeroMutableHashMap
    var planned = _defaultZeroMutableHashMap
    
    With.game.self.getUnits.asScala.groupBy(_.getType).foreach(pair => have.put(pair._1, pair._2.size))
  
    buildOrder.getUnitTypes.foreach(t => {
      need(t) += 1
      if(need(t) > have(t) + planned(t)) {
        _children.append(_createABuildPlan(t))
        planned(t) += 1
      }
    })
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
