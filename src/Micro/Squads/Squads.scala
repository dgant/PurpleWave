package Micro.Squads

import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Squads {
  
  var squads: ArrayBuffer[Squad] = new ArrayBuffer[Squad]
  var squadByUnit: mutable.Map[UnitInfo, Squad] = new mutable.HashMap[UnitInfo, Squad]
  
  def squadsByPriority: Seq[Squad] = squads.sortBy(_.client.priority)
  
  def reset() {
    squads.clear()
    squadByUnit.clear()
  }
  
  def commission(squad: Squad) {
    squads.append(squad)
    squad.recruits.foreach(squadByUnit(_) = squad)
  }
  
  def update() {
    squads.foreach(_.update())
  }
}
