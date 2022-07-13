package Planning.Compositions

import Lifecycle.With
import Planning.Plans.GamePlans.All.MacroActions
import Planning.Predicates.MacroCounting
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

import scala.collection.mutable.ArrayBuffer

class Composition(argUnits: CompositionUnit*) extends MacroActions with MacroCounting {
  val units = new ArrayBuffer[CompositionUnit]
  units ++= argUnits

  def pump(): Unit = {
    if (units.isEmpty) return
    val count = new CountMap[UnitClass]()
    val raxTs = units.map(_.unit.whatBuilds._1).distinct
    val raxes = raxTs.map(super.units(_)).sum
    var mins  = With.self.minerals
    var gas   = With.self.gas
    var dMins = With.accounting.ourIncomePerFrameMinerals
    var dGas  = With.accounting.ourIncomePerFrameGas
    units.foreach(u => count(u.unit) = super.units(u.unit))
    (0 until 3).foreach(cycle =>
      (0 until raxes).foreach(iRax => {
        // TODO: Weigh and pick

        var nextUnit = units.head.unit
        get(count(nextUnit),  nextUnit)
        count(nextUnit) += 1
      }))
  }
}
