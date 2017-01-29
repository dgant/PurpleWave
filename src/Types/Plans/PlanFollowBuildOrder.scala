package Types.Plans

import Processes.Architect
import Startup.With
import Types.Invoices.InvoiceUnits
import Types.BuildOrders.BuildOrder
import Types.Contracts.ContractUnits
import Types.Quantities.Exactly
import Types.Requirements.RequireCurrency
import Types.Tactics.{Tactic, TacticBuildUnit}
import UnitMatching.Matcher.UnitMatchType
import bwapi.{TilePosition, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PlanFollowBuildOrder extends Plan() {
  val buildOrder = new BuildOrder
  val existingBuilds:mutable.HashMap[ContractUnits, UnitType] = mutable.HashMap.empty
  
  var _children:List[Plan] = List.empty
  
  override def update() {
    if (_children.isEmpty) {
      _children = buildOrder.getUnitTypes.map(_buildBuildPlan).toList
    }
  }
  
  def _buildBuildPlan(product: UnitType):Plan = {
    val builder = product.whatBuilds.first
    if (builder.isBuilding) {
      return new PlanBuildUnitFromBuilding(builder, product)
    }
    if (builder.isWorker) {
      return new PlanBuildBuildingWithWorker(builder, product)
    }
    
    throw new Exception("Don't know how to build this yet.")
  }
  
  override def children(): Iterable[Plan] = _children

  override def execute(): Iterable[Tactic] = {
    val have = With.game.self.getUnits.asScala.groupBy(_.getType).mapValues(_.size)
    val need:mutable.HashMap[UnitType, Integer] = mutable.HashMap()
    val queue:ListBuffer[UnitType] = ListBuffer.empty

    existingBuilds.keys.filter(contract => contract.employees.size == 0).foreach(existingBuilds.remove)
    
    buildOrder.getUnitTypes.foreach(t => {
      if ( ! need.contains(t)) {
        need.put(t, 0)
      }
      need(t) += 1
      var haveN = 0
      if (have.contains(t)) {
        haveN = have(t)
      }
      if(need(t) > haveN + existingBuilds.groupBy(p => p._2).size) {
        queue.append(t)
      }
    })
    
    var output = queue.map(build).filterNot(_ == None).map(_.get)
    output
  }

  def build(unitType:UnitType): Option[Tactic] = {
    val invoice = new RequireCurrency(
      minerals = unitType.mineralPrice(),
      gas = unitType.gasPrice(),
      supply = unitType.supplyRequired())
        
    //This could be improved to use the reservation system
    if ( ! With.bank.tryToSpend(invoice)) {
      return None
    }
    
    var job = new InvoiceUnits(
      new Exactly(1),
      new UnitMatchType(unitType.whatBuilds().first))
    
    val contract = With.recruiter.source(job)
    if (contract == None) {
      return None
    }
    
    var position:Option[TilePosition] = None
    if (unitType.isBuilding) {
      position = Architect.placeBuilding(unitType, BWTA.getStartLocation(With.game.self).getTilePosition)
    }
    
    var output = contract.get.employees.take(1).map(unit => new TacticBuildUnit(unit, unitType, position)).headOption
    output
  }
  
  
}
